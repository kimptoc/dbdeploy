package net.sf.dbdeploy.database;

import net.sf.dbdeploy.ToPrintSteamDeployer;
import net.sf.dbdeploy.database.changelog.DatabaseSchemaVersionManager;
import net.sf.dbdeploy.database.syntax.DbmsSyntax;
import net.sf.dbdeploy.database.syntax.DbmsSyntaxFactory;
import net.sf.dbdeploy.exceptions.SchemaVersionTrackingException;
import net.sf.dbdeploy.scripts.ChangeScript;
import net.sf.dbdeploy.scripts.ChangeScriptRepository;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScriptGenerationTest {
	// TODO: the generated script should not contain a commit after the first insert into changelog
	// Why? becuase if it doesn't commit then, on failure, the script can just be
	//  reexecuted

	// TODO: need to test undo scripts as well as do scripts

	@Test
	public void generateConsolidatedChangesScriptForAllDatabasesAndCompareAgainstTemplate() throws Exception {
		for (String syntax : new DbmsSyntaxFactory().getSyntaxNames()) {
			try {
				System.out.printf("Testing syntax %s\n", syntax);
				runIntegratedTestAndConfirmOutputResults(syntax);
			} catch (Exception e) {
				throw new RuntimeException("Failed while testing syntax " + syntax, e);
			}
		}
	}

	private void runIntegratedTestAndConfirmOutputResults(String syntaxName) throws Exception {
		DbmsSyntax syntax = DbmsSyntax.createFor(syntaxName);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(outputStream);

		ChangeScript changeOne = new StubChangeScript(1, "1: change1.sql", "-- contents of change script 1");
		ChangeScript changeTwo = new StubChangeScript(2, "2: change2.sql", "-- contents of change script 2");

		List<ChangeScript> changeScripts = Arrays.asList(changeOne, changeTwo);
		ChangeScriptRepository changeScriptRepository = new ChangeScriptRepository(changeScripts);

		ToPrintSteamDeployer deployer = new ToPrintSteamDeployer(
				new StubSchemaManager(syntax), changeScriptRepository, printStream,
				syntax, null);

		deployer.doDeploy(Integer.MAX_VALUE);

		org.junit.Assert.assertEquals(readExpectedFileContents(getExpectedFilename(syntaxName)), outputStream.toString());
	}

	private String getExpectedFilename(String dbSyntaxName) {
		return dbSyntaxName + "_expected.sql";
	}

	private String readExpectedFileContents(String expectedFilename) throws IOException {
		final InputStream stream = getClass().getResourceAsStream(expectedFilename);
		assertThat("Did not find resource file called " + expectedFilename, stream, notNullValue());
		int size = stream.available();
		byte[] content = new byte[size];
		assertThat(stream.read(content), is(size));
		return new String(content);
	}


	private class StubSchemaManager extends DatabaseSchemaVersionManager {
		public StubSchemaManager(DbmsSyntax syntax) {
			super(null, syntax, null);
		}

		public List<Integer> getAppliedChangeNumbers() throws SchemaVersionTrackingException {
			return Collections.emptyList();
		}
	}

	private class StubChangeScript extends ChangeScript {
		private final String changeContents;

		public StubChangeScript(int changeNumber, String description, String changeContents) {
			super(changeNumber, description);
			this.changeContents = changeContents;
		}

		public String getContent() throws IOException {
			return changeContents;
		}
	}
}