package hudson.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;

/**
 *
 * @author Olivier Lamy
 * @since 3.0
 *
 */
public class MavenInformation implements Serializable {

    private static final long serialVersionUID = 8477909321273479507L;

    private final String version;

    private final String versionResourcePath;

    public MavenInformation(String version, String versionResourcePath) {
        this.version = version;
        this.versionResourcePath = versionResourcePath;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionResourcePath() {
        return versionResourcePath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MavenInformation{");
        sb.append("version='").append(version).append('\'');
        sb.append(", versionResourcePath='").append(versionResourcePath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
