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

package com.example.basicapp;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog;

    /**
     * Constructor of the class
     * @param myActivity Activity object
     * @since 1.0
     */
    public LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    /**
     * This method is used for initiating oa object of this class
     * @since 1.0
     */
    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_load_dialog, null));
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * This method is used for dismissing the dialog window
     * @since 1.0
     */
    void dismissDialog(){
        dialog.dismiss();
    }
}
