package cn.yhq.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment管理帮助类，提供Fragment的添加、切换与状态保存和恢复
 *
 * @author Yanghuiqiang 2015-10-13
 */
public final class FragmentHelper {
    private Context mContext;
    private List<TabInfo> mTabs = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private TabInfo mLastTab;
    private OnFragmentChangeListener mOnFragmentChangeListener;

    public void setOnFragmentChangeListener(OnFragmentChangeListener onFragmentChangeListener) {
        this.mOnFragmentChangeListener = onFragmentChangeListener;
    }

    public interface OnFragmentChangeListener {
        void onFragmentChanged(TabInfo tabInfo);
    }

    public static final class TabInfo {

        private final String tag;
        private final Class<? extends Fragment> clss;
        private final Bundle args;
        private Fragment fragment;

        TabInfo(String _tag, Class<? extends Fragment> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }

        /**
         * @return the fragment
         */
        public Fragment getFragment() {
            return fragment;
        }

        /**
         * @param fragment the fragment to set
         */
        public void setFragment(Fragment fragment) {
            this.fragment = fragment;
        }

        /**
         * @return the tag
         */
        public String getTag() {
            return tag;
        }

        /**
         * @return the clss
         */
        public Class<? extends Fragment> getClss() {
            return clss;
        }

        /**
         * @return the args
         */
        public Bundle getArgs() {
            return args;
        }


    }

    private FragmentHelper(Context context, FragmentManager fragmentManager, int containerId) {
        this.mContext = context;
        this.mFragmentManager = fragmentManager;
        this.mContainerId = containerId;
    }

    public static FragmentHelper setup(FragmentActivity activity, int containerId) {
        return new FragmentHelper(activity, activity.getSupportFragmentManager(), containerId);
    }

    public static FragmentHelper setup(Fragment fragment, int containerId) {
        return new FragmentHelper(fragment.getContext(), fragment.getChildFragmentManager(), containerId);
    }

    public void saveInstanceState(Bundle bundle) {
        if (this.mLastTab != null) {
            bundle.putString("curTab", this.mLastTab.tag);
        }
    }

    public void restoreInstanceState(Bundle bundle) {
        String curTab = bundle.getString("curTab");
        if (TextUtils.isEmpty(curTab)) {
            return;
        }
        changeFragment(curTab);
    }

    public void changeFragment(Class<? extends Fragment> fragmentClass) {
        changeFragment(fragmentClass.getName());
    }

    public <T extends Fragment> T getFragment(Class<T> clss) {
        return getFragment(clss.getName());
    }

    public int indexOf(Class<? extends Fragment> clss) {
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.clss == clss) return i;
        }
        return 0;
    }

    public TabInfo getLastTabInfo() {
        return this.mLastTab;
    }

    public int getFragmentContainerViewId() {
        return this.mContainerId;
    }

    @SuppressWarnings("unchecked")
    public <T extends Fragment> T getFragment(String tag) {
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = (TabInfo) mTabs.get(i);
            if (tab.tag.equals(tag)) return (T) tab.fragment;
        }
        return null;
    }

    public void changeFragment(int position) {
        Class<? extends Fragment> fragmentClass = this.mTabs.get(position).getClss();
        this.changeFragment(fragmentClass);
    }

    public void changeFragment(String tag) {
        FragmentTransaction ft = doChanged(tag, null);
        if (ft != null) ft.commit();
        if (mOnFragmentChangeListener != null) {
            TabInfo tabInfo = null;
            for (int i = 0; i < mTabs.size(); i++) {
                TabInfo tab = (TabInfo) mTabs.get(i);
                if (tab.tag.equals(tag)) tabInfo = tab;
            }
            mOnFragmentChangeListener.onFragmentChanged(tabInfo);
        }
    }

    private FragmentTransaction doChanged(String tag, FragmentTransaction ft) {
        TabInfo newTab = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = (TabInfo) mTabs.get(i);
            if (tab.tag.equals(tag)) newTab = tab;
        }

        if (newTab == null) throw new IllegalStateException(
                (new StringBuilder()).append("No tab known for tag ").append(tag).toString());
        if (mLastTab != newTab) {
            if (ft == null) ft = mFragmentManager.beginTransaction();
            if (mLastTab != null && mLastTab.fragment != null) ft.hide(mLastTab.fragment);
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = mFragmentManager.findFragmentByTag(tag);
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mContext, newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag).hide(newTab.fragment);
                    }
                } else {
                    ft.show(newTab.fragment);
                    mFragmentManager.executePendingTransactions();
                }
            }
            mLastTab = newTab;
        }
        return ft;
    }


    @SuppressWarnings("unchecked")
    private <T extends Fragment> T add(String tag, Class<T> clss, Bundle args) {
        if (mContainerId == 0) {
            throw new IllegalArgumentException(
                    "Fragment容器mContainerId不可为空，请覆盖getFragmentContainerViewId()方法。");
        }
        TabInfo info = new TabInfo(tag, clss, args);
        info.fragment = mFragmentManager.findFragmentByTag(tag);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (info.fragment == null || !info.fragment.isAdded()) {
            info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            ft.add(mContainerId, info.fragment, tag).hide(info.fragment);
            ft.commit();
        } else {
            ft.hide(info.fragment);
            ft.commit();
        }
        mTabs.add(info);
        return (T) info.fragment;
    }


    public <T extends Fragment> T addFragment(Class<T> fragment, Bundle args) {
        String tag = fragment.getName();
        return add(tag, fragment, args);
    }

    public <T extends Fragment> T addFragment(Class<T> fragment) {
        return addFragment(fragment, null);
    }

}
