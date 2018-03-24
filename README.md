# AndroidFragmentHelper

Fragment切换帮助类

##gradle配置方式
`compile 'cn.yhq:android-fragment-helper:1.2.0'`

##使用方式
###1、初始化
```java
FragmentHelper fragmentHelper = FragmentHelper.setup(this, R.id.fragment_container);
```

###2、添加Fragment
```java
fragmentHelper.addFragment(Fragment1.class);
fragmentHelper.addFragment(Fragment2.class);
```

###3、切换Fragment
```java
fragmentHelper.changeFragment(0);
fragmentHelper.changeFragment(Fragment1.class);
```