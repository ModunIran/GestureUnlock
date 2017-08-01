# GestureUnlock
A customized gesture view.
# V1.0 Commited on 2017/8/1
 The UnlockView contains all code.
 Thanks for using!
# Usage
1.Just copy the UnlockView.java to your project;</br>
2.Add this view into your layout</br>
```Java
    <com.wulei.gestureunlock.UnlockView
        android:id="@+id/unlock"
        android:layout_height="wrap_content"
        android:layout_width="match_parent" />
```
3.Find the view in code:</br>
```Java
mUnlockView= (UnlockView) findViewById(R.id.unlock);
```
4.Set the mode(CREATE_MODE:the mode for creating gesture;CHECK_MODE:the mode for verifing gesture):</br>

5.Add Listener:</br>
```Java
        //The listener for creating gesture;
        mUnlockView.setGestureListener(new UnlockView.CreateGestureListener() {
            @Override
            public void onGestureCreated(String result) {
                ...
            }
        });
        //The listener for verifing gesture;
        mUnlockView.setOnUnlockListener(new UnlockView.OnUnlockListener() {
            @Override
            public boolean isUnlockSuccess(String result) {
              ...
            }

            @Override
            public void onSuccess() {
                ...
            }

            @Override
            public void onFailure() {
                ...
            }
        });
```
6.Customize your gesture unlockview:</br>
```Java
mUnlockView.setErrorColor(Color.parseColor("#FF0000"));//Customize the color of error circles
mUnlockView.setNormalColor(Color.parseColor("#D5DBE8"));//Customize the color of normal circles
mUnlockView.setSelectColor(Color.parseColor("#508CEE"));//Customize the color of selected circles
mUnlockView.setPathWidth(3);//Customize the width of path
mUnlockView.setNormalR(20);//Customize the radius of inner circle
mUnlockView.setSelectR(30);//Customize the radius of outer circle
```
7.Enjoy it!

中文博客链接：[吴磊的简书](http://www.jianshu.com/p/a4c29ec5712f)


# How to contact me:
  QQ:331948214</br>
  Email:331948214@qq.com


