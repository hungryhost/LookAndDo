/* Copyright 2020 Yury Borodin. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.yuryborodin.lookanddo;

import android.graphics.Path;
public class FingerHandler {

    public int color;
    public Path path;
    public int strokeWidth;

    /**
     * Constructor of the class
     * @param color color, int
     * @param strokeWidth width of the stroke, int
     * @param path Path object
     */
    public FingerHandler(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
