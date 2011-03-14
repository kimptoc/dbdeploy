package com.dbdeploy.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.dbdeploy.DbDeploy;
import org.apache.maven.plugin.MojoExecutionException;


/**
 * Maven goal for applying dbdeploy change scripts directly to the database.
 *
 * @goal update
 */
public class UpdateDatabaseMojo extends AbstractDbDeployMojo {

    public void execute() throws MojoExecutionException {
        DbDeploy dbDeploy = getConfiguredDbDeploy();

        try {
            dbDeploy.go();
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("dbdeploy update failed", e);
        }
    }

}