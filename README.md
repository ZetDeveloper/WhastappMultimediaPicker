
#Multimedia Picker like wahtsapp

A simple library to select images and video from the gallery and camera.

![alt tag](http://i.imgur.com/AlvjdVt.jpg)

```java
 MultimediaPicker.create(this)
            .folderMode(true) // folder mode (false by default)
            .folderTitle("Folder") // folder selection title
            .imageTitle("Tap to select") // image selection title
            .setOnlyImages(true) //show only images tab
            .setOnlyVideos(true) //show only videos tab
            .single() // single mode
            .multi() // multi mode (default mode)
            .limit(10) // max images can be selected (999 by default)
            .showCamera(true) // show camera or not (true by default)
            .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
            .origin(images) // original selected images, used in multi mode
            .start(REQUEST_CODE_PICKER); // start image picker activity with request code
```

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
