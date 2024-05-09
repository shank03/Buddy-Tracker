package com.major.buddytracker.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.major.buddytracker.R
import com.major.buddytracker.data.BuddyPair
import com.major.buddytracker.data.UserModel
import com.major.buddytracker.ui.theme.BuddyTrackerTheme
import com.major.buddytracker.utils.Data

private const val TAG = "MainScreen"


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(onLogout: () -> Unit, onQrCode: () -> Unit) {

    val ctx = LocalContext.current

    val mUser = FirebaseAuth.getInstance().currentUser
    if (mUser == null) {
        onLogout()
        return
    }

    val db = FirebaseDatabase.getInstance()
        .getReference("buddyPairs")

    val bpList = SnapshotStateList<BuddyPair>()
    val userMap = SnapshotStateMap<String, UserModel>()

    db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                onLogout()
                return
            }

            bpList.clear()
            bpList.addAll(snapshot.children.filter {
                val bp = it.getValue(BuddyPair::class.java)
                bp?.buddy1Id == user.uid || bp?.buddy2Id == user.uid
            }.map { p ->
                val bp = p.getValue(BuddyPair::class.java)
                Log.d(TAG, "onDataChange: ${p.value}")
                if (bp != null) {
                    return@map bp
                }
                BuddyPair()
            }.filter { bp ->
                bp.buddy1Id != ""
            })

            bpList.forEach { bp ->
                val id = if (bp.buddy1Id == user.uid) bp.buddy2Id else bp.buddy1Id
                FirebaseDatabase.getInstance().getReference("Users").child(id).get()
                    .addOnSuccessListener { p ->
                        val pUser = p.getValue(UserModel::class.java)
                        if (pUser != null) {
                            userMap[bp.buddy1Id] = pUser
                        }
                    }
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })

    val state = rememberLazyListState()

    BuddyTrackerTheme {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(text = "Hello ${mUser.email}")

                Button(
                    onClick = {
                        onLogout()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Log out")
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 56.dp),
                    state = state
                ) {
                    items(bpList.size) {
                        val id =
                            if (bpList[it].buddy1Id == mUser.uid) bpList[it].buddy2Id else bpList[it].buddy1Id
                        Card(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterVertically)
                                )

                                Spacer(modifier = Modifier.padding(4.dp))

                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    Text(
                                        text = userMap[id]?.userName ?: "...",
                                    )
                                    Spacer(modifier = Modifier.padding(2.dp))

                                    Text(
                                        text = "Start: ${bpList[it].startDateTime}",
                                        fontSize = 12.sp
                                    )
                                    Text(text = "End: ${bpList[it].endDateTime}", fontSize = 12.sp)
                                    Text(text = bpList[it].buddyPairId, fontSize = 11.sp)
                                }

                                if (bpList[it].locationMapAddress.isNotEmpty()) {
                                    IconButton(onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(bpList[it].locationMapAddress)
                                        )
                                        ctx.startActivity(intent)
                                    }, modifier = Modifier.align(Alignment.CenterVertically)) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.rounded_near_me_24),
                                            contentDescription = ""
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    Data.ID_DATA = if (bpList[it].buddy1Id == mUser.uid) 2 else 1
                                    Data.LOCATION_ADR = bpList[it].locationCode
                                    Data.PAIR_ID = bpList[it].buddyPairId
                                    onQrCode()
                                }, modifier = Modifier.align(Alignment.CenterVertically)) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                    }
                    item {
                        if (bpList.isEmpty()) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
