# SevenSegmentTimer  
  An Android custom timer view that imitates the 7-Segment Display.  

  ![demo gif](https://github.com/lynnzc/SevenSegmentTimer/blob/master/demo.gif)  

# Attribute

  - timerBgColor : change timer background color  
  - timerOnColor : highlight color for timer digit 
  - timerOffColor : default color for timer digit  
  - separatorBgColor : separator view background color  
  - separatorColor : separator view color  

# Usage  

  use additional attributes in your layout file  
	
	<com.lynn.code.sevensegmenttimer.CountDownDigitTimer
        android:id="@+id/digit_view"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_centerInParent="true"
        app:separatorColor="@color/colorAccent"
        app:timerBgColor="#000000"
        app:timerOffColor="@color/material_grey_50"
        app:timerOnColor="@color/colorPrimary" />  
	
  initialize the timer  
	
	CountDownDigitTimer v = (CountDownDigitTimer) findViewById(R.id.digit_view);  
	
	v.setCountDownTime(99, 59, 59);  
	


# License  
    
	Copyright (C) 2016, Lynn

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
    
