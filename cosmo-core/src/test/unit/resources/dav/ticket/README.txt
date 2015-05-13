until automated dav tests are in place, you can use the xml files in this
directory as content for requests sent with curl, like so:

curl -X MKTICKET -u root:cosmo -H 'Content-Type: text/xml; charset="utf-8"' -d @mkticket.xml http://localhost:8080/home/

curl -X DELTICKET -u root:cosmo -H 'Ticket: deadbeef' http://localhost:8080/home/




Copyright 2006 Open Source Applications Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
