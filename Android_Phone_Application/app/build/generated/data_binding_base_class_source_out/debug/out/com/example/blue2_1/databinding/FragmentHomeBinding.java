// Generated by view binder compiler. Do not edit!
package com.example.blue2_1.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.blue2_1.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentHomeBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView bluetooth;

  @NonNull
  public final Button changeValBut;

  @NonNull
  public final Button closeBT;

  @NonNull
  public final TextView textHome;

  private FragmentHomeBinding(@NonNull ConstraintLayout rootView, @NonNull TextView bluetooth,
      @NonNull Button changeValBut, @NonNull Button closeBT, @NonNull TextView textHome) {
    this.rootView = rootView;
    this.bluetooth = bluetooth;
    this.changeValBut = changeValBut;
    this.closeBT = closeBT;
    this.textHome = textHome;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentHomeBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentHomeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_home, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentHomeBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.bluetooth;
      TextView bluetooth = ViewBindings.findChildViewById(rootView, id);
      if (bluetooth == null) {
        break missingId;
      }

      id = R.id.changeValBut;
      Button changeValBut = ViewBindings.findChildViewById(rootView, id);
      if (changeValBut == null) {
        break missingId;
      }

      id = R.id.closeBT;
      Button closeBT = ViewBindings.findChildViewById(rootView, id);
      if (closeBT == null) {
        break missingId;
      }

      id = R.id.text_home;
      TextView textHome = ViewBindings.findChildViewById(rootView, id);
      if (textHome == null) {
        break missingId;
      }

      return new FragmentHomeBinding((ConstraintLayout) rootView, bluetooth, changeValBut, closeBT,
          textHome);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
