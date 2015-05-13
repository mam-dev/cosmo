You can send CalDAV requests with curl, like so:

curl -i -X MKCALENDAR -u test1:test1 http://localhost:8080/cosmo/home/test1/testcalendar

curl -X MKCALENDAR -u test1:test1 -v -H 'Content-Type: text/xml; charset="utf-8"' -d @mkcalendar.xml http://localhost:8080/cosmo/home/test1/testcalendar




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
