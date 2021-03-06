/*
 * Copyright (c) 2013 IRCCloud, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irccloud.android.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.irccloud.android.NetworkConnection;
import com.irccloud.android.R;
import com.irccloud.android.activity.EditConnectionActivity;
import com.irccloud.android.data.ServersDataSource;

import java.util.ArrayList;

public class AddChannelFragment extends DialogFragment {
    SparseArray<ServersDataSource.Server> servers;
    Spinner spinner;
    TextView channels;
    int defaultCid = -1;

    class DoneClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            int pos = spinner.getSelectedItemPosition();
            if (pos >= 0 && pos < servers.size()) {
                int cid = servers.valueAt(spinner.getSelectedItemPosition()).cid;
                String[] splitchannels = channels.getText().toString().split(",");
                for (int i = 0; i < splitchannels.length; i++) {
                    String[] channelandkey = splitchannels[i].split(" ");
                    if (channelandkey.length > 1)
                        NetworkConnection.getInstance().join(cid, channelandkey[0].trim(), channelandkey[1]);
                    else
                        NetworkConnection.getInstance().join(cid, channelandkey[0].trim(), "");
                }
                dismiss();
            }
        }
    }

    public void setDefaultCid(int cid) {
        defaultCid = cid;
    }

    @Override
    public void onResume() {
        int pos = 0;
        super.onResume();
        servers = ServersDataSource.getInstance().getServers();

        ArrayList<String> servernames = new ArrayList<String>();
        for (int i = 0; i < servers.size(); i++) {
            servernames.add(servers.valueAt(i).name);
            if (servers.valueAt(i).cid == defaultCid)
                pos = i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1, servernames.toArray(new String[servernames.size()]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(pos);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context ctx = getActivity();
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.dialog_add_channel, null);
        spinner = (Spinner) v.findViewById(R.id.networkSpinner);
        channels = (TextView) v.findViewById(R.id.channels);
        channels.setText("");
        channels.append("#");
        Button b = (Button) v.findViewById(R.id.addBtn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getActivity().getResources().getBoolean(R.bool.isTablet)) {
                    Intent i = new Intent(getActivity(), EditConnectionActivity.class);
                    startActivity(i);
                } else {
                    EditConnectionFragment newFragment = new EditConnectionFragment();
                    newFragment.show(getActivity().getSupportFragmentManager(), "editconnection");
                }
            }
        });

        return new AlertDialog.Builder(ctx)
                .setTitle("Join A Channel")
                .setInverseBackgroundForced(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                .setView(v)
                .setPositiveButton("Join", new DoneClickListener())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
