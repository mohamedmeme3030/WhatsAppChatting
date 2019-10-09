package com.example.khaled_sa2r.whatsappchatting.Adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.khaled_sa2r.whatsappchatting.Fragments.ChatFragment;
import com.example.khaled_sa2r.whatsappchatting.Fragments.ContactsFragment;
import com.example.khaled_sa2r.whatsappchatting.Fragments.GroupFragment;
import com.example.khaled_sa2r.whatsappchatting.Fragments.RequestFragment;

public class TabsAccessonAdapter extends FragmentPagerAdapter {
    public TabsAccessonAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i)
    {
        switch (i)
        {
            case 0 :
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1 :
                GroupFragment groupFragment= new GroupFragment();
                return groupFragment;
            case 2 :
                ContactsFragment contactsFragment= new ContactsFragment();
                return contactsFragment;

            case 3 :
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

                default:
                    return null;
        }
    }

    @Override
    public int getCount()
    {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0 :
                return "Chats";
            case 1 :
                return "Groups";
            case 2 :
                return "Contacts";
            case 3 :
                return "Requests";
            default:
                return null;
        }
    }
}
