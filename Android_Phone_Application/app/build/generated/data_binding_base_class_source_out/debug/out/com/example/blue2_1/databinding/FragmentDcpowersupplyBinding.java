// Generated by view binder compiler. Do not edit!
package com.example.blue2_1.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public final class FragmentDcpowersupplyBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button dcpowerselect;

  @NonNull
  public final EditText dcpowersupplyValue;

  @NonNull
  public final TextView textDcpowersupply;

  private FragmentDcpowersupplyBinding(@NonNull ConstraintLayout rootView,
      @NonNull Button dcpowerselect, @NonNull EditText dcpowersupplyValue,
      @NonNull TextView textDcpowersupply) {
    this.rootView = rootView;
    this.dcpowerselect = dcpowerselect;
    this.dcpowersupplyValue = dcpowersupplyValue;
    this.textDcpowersupply = textDcpowersupply;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentDcpowersupplyBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentDcpowersupplyBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_dcpowersupply, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentDcpowersupplyBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.dcpowerselect;
      Button dcpowerselect = ViewBindings.findChildViewById(rootView, id);
      if (dcpowerselect == null) {
        break missingId;
      }

      id = R.id.dcpowersupplyValue;
      EditText dcpowersupplyValue = ViewBindings.findChildViewById(rootView, id);
      if (dcpowersupplyValue == null) {
        break missingId;
      }

      id = R.id.text_dcpowersupply;
      TextView textDcpowersupply = ViewBindings.findChildViewById(rootView, id);
      if (textDcpowersupply == null) {
        break missingId;
      }

      return new FragmentDcpowersupplyBinding((ConstraintLayout) rootView, dcpowerselect,
          dcpowersupplyValue, textDcpowersupply);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
