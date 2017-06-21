## This is forked branch from ![SwipeRevealLayout](https://github.com/chthai64/SwipeRevealLayout) with multiple horizontal edges supported.

### Usage swipeLayout.setEnableEdges( EdgeRight | EdgeLeft)

### Installation 
Currently still using the latest commit from master.

repositories {
    maven {
        url "https://jitpack.io"
    }
}

compile 'com.github.hydrated:SwipeRevealLayout:c3a9ddc'


### Demo
##### Overview

SwipeRevealLayout/art/demo_all.gif
![Demo all](https://raw.githubusercontent.com/hydrated/SwipeRevealLayout/master/art/ezgif-1-f6e2694ce0.gif)

```app:minDistRequestDisallowParent```: The minimum distance (in px or dp) to the closest drag edge that the SwipeRevealLayout will disallow the parent to intercept touch event. It basically means the minimum distance to swipe until a RecyclerView (or something similar) cannot be scrolled.

```setSwipeListener(SwipeListener swipeListener)```: set the listener for the layout. You can use the full interface ```SwipeListener``` or a simplified listener class ```SimpleSwipeListener```

```open(boolean animation)```, ```close(boolean animation)```: open/close the layout. If ```animation``` is set to false, the listener will not be called.

```isOpened()```, ```isClosed()```: check if the layout is fully opened or closed.

```setMinFlingVelocity(int velocity)```: set the minimum fling velocity (dp/sec) to cause the layout to open/close.

```setEnableEdge(int edge)```: Enable the edge where the layout can be dragged from.

```setLockDrag(boolean lock)```: If set to true, the user cannot drag/swipe the layout.

```viewBinderHelper.lockSwipe(String... id), viewBinderHelper.unlockSwipe(String... id)```: Lock/unlock layouts which are binded to the binderHelper.

```viewBinderHelper.setOpenOnlyOne(boolean openOnlyOne)```: If ```openOnlyOne``` is set to true, you can only open one row at a time.

```viewBinderHelper.openLayout(String id)```: Open a layout. ```id``` is the id of the data object which is bind to the layout.

```viewBinderHelper.closeLayout(String id)```: Close a layout. ```id``` is the id of the data object which is bind to the layout.

### License
```
 The MIT License (MIT)

 Copyright (c) 2016 Chau Thai, Hydra

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
