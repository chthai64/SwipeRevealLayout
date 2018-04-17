## SwipeRevealLayout
A layout that you can swipe/slide to show another layout.

### Demo
##### Overview
![Demo all](https://raw.githubusercontent.com/chthai64/SwipeRevealLayout/master/art/demo_all.gif)

##### Drag mode

Drag mode normal:   
![Demo normal](https://raw.githubusercontent.com/chthai64/SwipeRevealLayout/master/art/demo_normal.gif)

Drag mode same_level:   
![Demo same](https://raw.githubusercontent.com/chthai64/SwipeRevealLayout/master/art/demo_same.gif)

### Features
* Flexible, easy to use with RecyclerView, ListView or any view that requires view binding.
* Four drag edges (left, right, top, bottom).
* Two drag modes:
  * Normal (the secondary view is underneath the main view).
  * Same level (the secondary view sticks to the edge of the main view).
* Able to open one row at a time.
* Minimum api level 9.

### Usage
#### Dependencies
```groovy
dependencies {
    compile 'com.chauthai.swipereveallayout:swipe-reveal-layout:1.4.1'
}
```

#### Layout file
```xml
<com.chauthai.swipereveallayout.SwipeRevealLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:mode="same_level"
        app:dragEdge="left">

        <!-- Your secondary layout here -->
        <FrameLayout
            android:layout_width="wrap_content"
            android:layout_height="match_parent" />

        <!-- Your main layout here -->
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
            
</com.chauthai.swipereveallayout.SwipeRevealLayout>
```
```app:mode``` can be ```normal``` or ```same_level```

```app:dragEdge``` can be ```left```, ```right```, ```top``` or ```bottom```

#### Use with RecyclerView, ListView, GridView...
##### In your Adapter class:
```java
public class Adapter extends RecyclerView.Adapter {
  // This object helps you save/restore the open/close state of each view
  private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
  
  public Adapter() {
    // uncomment the line below if you want to open only one row at a time
    // viewBinderHelper.setOpenOnlyOne(true);
  }
  
  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // get your data object first.
    YourDataObject dataObject = mDataSet.get(position); 
    
    // Save/restore the open/close state.
    // You need to provide a String id which uniquely defines the data object.
    viewBinderHelper.bind(holder.swipeRevealLayout, dataObject.getId()); 

    // do your regular binding stuff here
  }
}
```

##### Optional, to restore/save the open/close state when the device's orientation is changed:
##### Adapter class:
```java
public class YourAdapter extends RecyclerView.Adapter {
  ...

  public void saveStates(Bundle outState) {
      viewBinderHelper.saveStates(outState);
  }

  public void restoreStates(Bundle inState) {
      viewBinderHelper.restoreStates(inState);
  }  
}
```
##### Activity class:
```java
public class YourActivity extends Activity {
  ...
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      if (adapter != null) {
          adapter.saveStates(outState);
      }
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      if (adapter != null) {
          adapter.restoreStates(savedInstanceState);
      }
  }
}
```

#### Useful Methods/Attributes
```app:minDistRequestDisallowParent```: The minimum distance (in px or dp) to the closest drag edge that the SwipeRevealLayout will disallow the parent to intercept touch event. It basically means the minimum distance to swipe until a RecyclerView (or something similar) cannot be scrolled.

```setSwipeListener(SwipeListener swipeListener)```: set the listener for the layout. You can use the full interface ```SwipeListener``` or a simplified listener class ```SimpleSwipeListener```

```open(boolean animation)```, ```close(boolean animation)```: open/close the layout. If ```animation``` is set to false, the listener will not be called.

```isOpened()```, ```isClosed()```: check if the layout is fully opened or closed.

```setMinFlingVelocity(int velocity)```: set the minimum fling velocity (dp/sec) to cause the layout to open/close.

```setDragEdge(int edge)```: Change the edge where the layout can be dragged from.

```setLockDrag(boolean lock)```: If set to true, the user cannot drag/swipe the layout.

```viewBinderHelper.lockSwipe(String... id), viewBinderHelper.unlockSwipe(String... id)```: Lock/unlock layouts which are binded to the binderHelper.

```viewBinderHelper.setOpenOnlyOne(boolean openOnlyOne)```: If ```openOnlyOne``` is set to true, you can only open one row at a time.

```viewBinderHelper.openLayout(String id)```: Open a layout. ```id``` is the id of the data object which is bind to the layout.

```viewBinderHelper.closeLayout(String id)```: Close a layout. ```id``` is the id of the data object which is bind to the layout.

### License
```
 The MIT License (MIT)

 Copyright (c) 2016 Chau Thai

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
```
