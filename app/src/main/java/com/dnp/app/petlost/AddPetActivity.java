package com.dnp.app.petlost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class AddPetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        Intent intent = this.getIntent();
        if (intent == null){
            Log.d("Tag", "La actividad no se ha llamado mediante un intent.");
        }

    }

}
