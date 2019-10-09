package com.example.khaled_sa2r.whatsappchatting.MainProject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khaled_sa2r.whatsappchatting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilActivity extends AppCompatActivity {

    private String receiverUserID,Current_State,senderUserID;
    private CircleImageView userProfilImage;
    private TextView userProfilName,userProfilStatus;
    private Button SendMessageRequestButton,DeclineMessageRequestButton;
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID =mAuth.getCurrentUser().getUid();
//      Toast.makeText(this, "User ID: "+ receiverUserID, Toast.LENGTH_SHORT).show();

        userProfilImage = (CircleImageView) findViewById(R.id.visit_profil_image);
        userProfilName = (TextView) findViewById(R.id.visit_user_name);
        userProfilStatus = (TextView) findViewById(R.id.visit_profil_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);

        Current_State = "new";


                RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
      UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot)
          {
              if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
              {
                  String userImage = dataSnapshot.child("image").getValue().toString();
                  String userName = dataSnapshot.child("name").getValue().toString();
                  String userStatus = dataSnapshot.child("status").getValue().toString();

                  Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfilImage);
                  userProfilName.setText(userName);
                  userProfilStatus.setText(userStatus);

                  ManageChatRequest();
              }
              else
              {
                  String userName = dataSnapshot.child("name").getValue().toString();
                  String userStatus = dataSnapshot.child("status").getValue().toString();

                  userProfilName.setText(userName);
                  userProfilStatus.setText(userStatus);

                  ManageChatRequest();

              }
          }
          @Override
          public void onCancelled(@NonNull DatabaseError databaseError)
          {

          }
      });
    }

    private void ManageChatRequest()
    {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID)
                                    .child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                              Current_State = "request_received";
                              SendMessageRequestButton.setText("Accept Chat Request");

                              DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                              DeclineMessageRequestButton.setEnabled(true);
                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v)
                                  {
                                      Log.v("aaaaaaaaaaaa","click");
                                      CancelChatRequest();
                                  }
                              });
                            }
                        }
                        else
                        {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                Current_State = "friends";
                                                SendMessageRequestButton.setText("Remove This Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

        if (!senderUserID.equals(receiverUserID))
        {
              SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      SendMessageRequestButton.setEnabled(false);
                      if (Current_State.equals("new"))
                      {
                          SendChatRequest();
                      }
                      if (Current_State.equals("request_sent"))
                      {
                          CancelChatRequest();
                      }
                      if (Current_State.equals("request_received"))
                      {
                          AcceptChatRequest();
                      }
                      if (Current_State.equals("friends"))
                      {
                          RemoveSpecificContact();
                      }
                  }
              });
        }
        else
        {
            SendMessageRequestButton.setVisibility(View.GONE);
        }
    }

    private void RemoveSpecificContact()
    {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {

                if (task.isSuccessful())
                {
                    ContactsRef.child(receiverUserID).child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                SendMessageRequestButton.setEnabled(true);
                                Current_State="new";
                                SendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.GONE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest()
    {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                  ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                          .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                      @Override
                                                      public void onComplete(@NonNull Task<Void> task)
                                                      {
                                                          if (task.isSuccessful())
                                                          {
                                                              ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                      .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task)
                                                                  {
                                                                      SendMessageRequestButton.setEnabled(true);
                                                                      Current_State = "friend";
                                                                      SendMessageRequestButton.setText("Remove this Contact");
                                                                      DeclineMessageRequestButton.setVisibility(View.GONE);
                                                                      DeclineMessageRequestButton.setEnabled(false);
                                                                  }
                                                              });
                                                          }
                                                      }
                                                  });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {

                if (task.isSuccessful())
                {
                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                SendMessageRequestButton.setEnabled(true);
                                Current_State="new";
                                SendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.GONE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "request_sent";
                                                SendMessageRequestButton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
