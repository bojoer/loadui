// 
// Copyright 2013 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 
import java.util.zip.ZipFile

blacklist = []
packages = []

def lib = new File('C:\\Program Files\\Java\\jdk1.7.0_06\\jre\\lib')
scanFile( new File(lib, 'jfxrt.jar') )

def void scanFile(file) {
    if(!file.name.endsWith('.jar'))
        return
        
    def zip = new ZipFile(file)
    for(e in zip.entries()) {
        if(e.name.endsWith('.class')) {
            def name = e.name.substring(0, e.name.lastIndexOf('/')).replaceAll('/', '.')
            def ignore = false
            for(prefix in blacklist) {
                if(name.startsWith(prefix)) {
                    ignore = true
                    break
                }
            }
            if(!ignore)
                packages << name
        }
    }
}

new File('src/main/resources/packages-extra.txt').write(new TreeSet(packages).join(','))