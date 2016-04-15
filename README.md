## SwipeRevealLayout
A layout that you can swipe/slide to show another layout.

### Features
* Flexible, easy to use with RecyclerView, ListView or any view that requires view binding.
* Four drag edges (left, right, top, bottom).
* Two drag modes:
  * Normal (the secondary view is underneath the main view).
  * Same level (the secondary view sticks to the edge of the main view).

### Demo
##### Overview
![Demo all](https://raw.githubusercontent.com/chthai64/SwipeRevealLayout/master/art/demo_all.gif)

##### Drag mode

Drag mode normal:   
![Demo normal](https://raw.githubusercontent.com/chthai64/SwipeRevealLayout/master/art/demo_normal.gif)

Drag mode same_level:   
![Demo same](https://raw.githubusercontent.com/chthai64/SwipeRevealLayout/master/art/demo_same.gif)

### Usage
#### Dependencies
```groovy
repositories {
    maven {
        url 'https://dl.bintray.com/chthai64/maven/'
    }
}
```

```groovy
dependencies {
    compile 'com.chauthai.swipereveallayout:swipe-reveal-layout:1.0.0'
}
```

#### Layout file
```xml
<com.chauthai.swipereveallayout.SwipeRevealLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:mode="same_level"
        app:dragEdge="left">

        <!-- Your main layout here -->
        <FrameLayout
            android:layout_width="wrap_content"
            android:layout_height="match_parent" />

        <!-- Your secondary layout here -->
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
public class Adapter extends RecyclerView.Adapter {
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
