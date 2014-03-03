/*
 * Copyright 2007 Peter Ledbrook.
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
 *
 *
 *
 * Modified 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * Modified 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
 */

includeTargets << grailsScript("_GrailsArgParsing")

USAGE = """
    create-cas-realm [--prefix=PREFIX]

where
    PREFIX = The prefix to add to the name of the realm. This may include a
             package. (default: "Shiro").
"""

target(createCasRealm: "Creates a new CAS realm.") {
    def (pkg, prefix) = parsePrefix()

    // Copy over the template LDAP realm.
    def className = "${prefix}CasRealm"
    installTemplateEx("${className}.groovy", "grails-app/realms${packageToPath(pkg)}", "realms", "ShiroCasRealm.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
            ant.replacefilter(token: '@realm.name@', value: className)
        }
    }

    event("CreatedArtefact", ['Realm', className])
}

target (default: "Creates a CAS Shiro realm") {
    // Make sure any arguments have been parsed.
    depends(parseArguments, createCasRealm)
}
