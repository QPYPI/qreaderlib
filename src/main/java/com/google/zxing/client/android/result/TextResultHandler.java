/*
 * Copyright (C) 2008 ZXing authors
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

package com.google.zxing.client.android.result;

import com.google.zxing.Result;
import com.google.zxing.client.android.R;
import com.google.zxing.client.result.ParsedResult;
import com.quseit.base.MyApp;
import com.quseit.config.CONF;
import com.quseit.util.FileHelper;
import com.quseit.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * This class handles TextParsedResult as well as unknown formats. It's the fallback handler.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class TextResultHandler extends ResultHandler {

  private final String TAG = "TextResultHandler";
  Activity activity;
  private static final int[] buttons = {
          R.string.button_edit,
          R.string.button_run_with_py,
          R.string.button_run_as_parameter,
          R.string.button_custom_product_search,


/*      R.string.button_web_search,
      R.string.button_share_by_email,
      R.string.button_share_by_sms,
      R.string.button_custom_product_search,
      */
  };

  public TextResultHandler(Activity activity, ParsedResult result, Result rawResult) {
    super(activity, result, rawResult);
    this.activity = activity;

  }

  @Override
  public int getButtonCount() {
    return hasCustomProductSearch() ? buttons.length : buttons.length - 1;
  }

  @Override
  public int getButtonText(int index) {
    return buttons[index];
  }

  @Override
  public void handleButtonPress(int index) {
    String text = getResult().getDisplayResult();
    switch (index) {
      case 0:
        viewScript(text);
        //webSearch(text);
        break;
      case 1:
        runScript(text);
        //shareByEmail(text);
        break;
      case 2:
        runAsParameter(text);
        //shareBySMS(text);
        break;
      case 3:
        //openURL(fillInCustomSearchURL(text));
        break;
    }
  }

  @Override
  public int getDisplayTitle() {
    return R.string.result_text;
  }



  public File getCodeAsFile(String text, String tmp) {
    try {
      File pyCache = FileHelper.getBasePath(Environment.getExternalStorageDirectory().getAbsolutePath(),"qpython/cache");
      File py = new File(pyCache+"/"+tmp);

      byte[] data = text.getBytes();
      FileOutputStream outStream = null;

      outStream = new FileOutputStream(py);
      outStream.write(data);
      outStream.close();

      return py;

    } catch (FileNotFoundException e) {
      Toast.makeText(this.activity.getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
      e.printStackTrace();
    } catch (IOException e) {
      Toast.makeText(this.activity.getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

      e.printStackTrace();

    }
    return null;

  }

  public void runAsParameter(String text) {

    Log.d(TAG, "runAsParameter");

    File py = getCodeAsFile(text,".last_param");

    Intent it = new Intent();
    it.setClassName(this.activity.getPackageName(), "org.qpython.qpylib.MPyApi");
    it.setAction(Intent.ACTION_PICK);

    Uri uri = Uri.fromFile(py);

    it.setDataAndType(uri, "text/x-python");
    this.activity.startActivity(it);


  }

  public void runScript(String text) {
    Log.d(TAG, "runScript");

    File py = getCodeAsFile(text, ".last_tmp.py");

    Intent it = new Intent();
    it.setClassName(this.activity.getPackageName(), "org.qpython.qpylib.MPyApi");
    it.setAction(Intent.ACTION_RUN);

    Uri uri = Uri.fromFile(py);

    it.setDataAndType(uri, "text/x-python");
    this.activity.startActivity(it);


  }

  public void viewScript(String text) {
    Log.d(TAG, "viewScript");

    File py = getCodeAsFile(text, ".last_tmp.py");

    Intent it = new Intent();
    it.setClassName(this.activity.getPackageName(), "com.quseit.texteditor.TedActivity");
    it.setAction(Intent.ACTION_VIEW);

    Uri uri = Uri.fromFile(py);

    it.setDataAndType(uri, "text/x-python");
    this.activity.startActivity(it);

    //Toast.makeText(getApplicationContext(), MessageFormat.format(getString(R.string.saved_as), pyCache.getName()), Toast.LENGTH_SHORT).show();
  }
}
