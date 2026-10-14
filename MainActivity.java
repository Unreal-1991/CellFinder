package com.example.cellidfinder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyCallback;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.content.Context;
import android.location.LocationManager;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int REQ = 41;
    private TextView result;
    private TelephonyManager tm;
    private Button refresh;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(10, 20, 15));
        getWindow().setNavigationBarColor(Color.rgb(10, 20, 15));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 24, 22, 18);
        root.setBackgroundColor(Color.rgb(10, 16, 13));

        TextView title = new TextView(this);
        title.setText("5G CELL ID FINDER"); title.setTextSize(25); title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(80, 255, 145)); title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView subtitle = new TextView(this);
        subtitle.setText("Cellular details ng SIM na gamit ng phone"); subtitle.setTextColor(Color.LTGRAY); subtitle.setTextSize(14); subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.setMargins(0, 8, 0, 18); root.addView(subtitle, sp);

        refresh = new Button(this); refresh.setText("BASAHIN ULIT"); refresh.setOnClickListener(v -> readCells()); root.addView(refresh, new LinearLayout.LayoutParams(-1, -2));
        ScrollView sc = new ScrollView(this);
        result = new TextView(this); result.setTextColor(Color.WHITE); result.setTextSize(16); result.setPadding(8, 18, 8, 18);
        sc.addView(result); LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, 0, 1); rp.setMargins(0, 10, 0, 0); root.addView(sc, rp);
        TextView foot = new TextView(this); foot.setText("Paalala: maaaring itago ng Android/firmware o carrier ang 5G NCI/TAC. Ang 5G NSA ay maaaring magpakita ng LTE anchor."); foot.setTextColor(Color.rgb(180, 190, 185)); foot.setTextSize(12); root.addView(foot);
        setContentView(root);
        tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (!hasPermissions()) requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ);
        else readCells();
    }

    private boolean hasPermissions() {
        return checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
               checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
               checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(requestCode, permissions, grants);
        if (requestCode == REQ) {
            if (hasPermissions()) readCells();
            else result.setText("Kailangan ang Phone at Location permissions para subukang basahin ang cell information. Pumunta sa App info > Permissions kung gusto mong subukan ulit.");
        }
    }

    private void readCells() {
        if (!hasPermissions()) { requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ); return; }
        result.setText("Kinukuha ang cellular information...\n\nSiguraduhing naka-on ang SIM at Location.");
        try {
            tm.requestCellInfoUpdate(getMainExecutor(), new TelephonyManager.CellInfoCallback() {
                @Override public void onCellInfo(List<CellInfo> cells) {
                    StringBuilder out = new StringBuilder();
                    out.append("NETWORK: ").append(tm.getDataNetworkType()).append(" (numeric Android type)\n");
                    out.append("REGISTERED = kasalukuyang registered cell\n\n");
                    if (cells == null || cells.isEmpty()) {
                        out.append("Walang cell data na ibinalik. Maaaring limitado ng firmware/carrier ang access.");
                    } else {
                        int count = 0;
                        for (CellInfo ci : cells) {
                            count++;
                            out.append("CELL ").append(count).append(ci.isRegistered() ? "  [REGISTERED]" : "").append("\n");
                            if (Build.VERSION.SDK_INT >= 29 && ci instanceof CellInfoNr) {
                                CellIdentityNr id = (CellIdentityNr)((CellInfoNr)ci).getCellIdentity();
                                out.append("Radio: 5G NR\n");
                                out.append("NCI / Cell ID: ").append(validLong(id.getNci())).append("\n");
                                out.append("TAC: ").append(validInt(id.getTac())).append("\n");
                                out.append("PCI: ").append(validInt(id.getPci())).append("\n");
                                out.append("NR-ARFCN: ").append(validInt(id.getNrarfcn())).append("\n");
                                out.append("MCC/MNC: ").append(safe(id.getMccString())).append("/").append(safe(id.getMncString())).append("\n");
                            } else if (ci instanceof CellInfoLte) {
                                CellIdentityLte id = ((CellInfoLte)ci).getCellIdentity();
                                out.append("Radio: 4G LTE (posibleng anchor ng 5G NSA)\n");
                                out.append("ECI / LTE Cell ID: ").append(validInt(id.getCi())).append("\n");
                                out.append("TAC: ").append(validInt(id.getTac())).append("\n");
                                out.append("PCI: ").append(validInt(id.getPci())).append("\n");
                                out.append("EARFCN: ").append(validInt(id.getEarfcn())).append("\n");
                            } else if (ci instanceof CellInfoWcdma) {
                                android.telephony.CellIdentityWcdma id = ((CellInfoWcdma)ci).getCellIdentity();
                                out.append("Radio: 3G WCDMA\nLAC: ").append(validInt(id.getLac())).append("\nCID: ").append(validInt(id.getCid())).append("\n");
                            } else if (ci instanceof CellInfoGsm) {
                                android.telephony.CellIdentityGsm id = ((CellInfoGsm)ci).getCellIdentity();
                                out.append("Radio: GSM\nLAC: ").append(validInt(id.getLac())).append("\nCID: ").append(validInt(id.getCid())).append("\n");
                            } else out.append("Radio: ").append(ci.getClass().getSimpleName()).append("\n");
                            out.append("\n");
                        }
                        out.append("Tip: hanapin ang [REGISTERED]. Kung LTE lang ang lumabas habang 5G ang icon, maaaring 5G NSA o hindi inilalantad ng phone ang NR cell.");
                    }
                    result.setText(out.toString());
                }
                @Override public void onError(int errorCode, Throwable e) {
                    result.setText("Hindi mabasa ang cell information. Error code: " + errorCode + "\n\nSubukan: i-on ang Location, tiyaking may SIM signal, at pindutin ang BASAHIN ULIT. Maaaring hindi payagan ng firmware ang 5G details.");
                }
            });
        } catch (SecurityException e) {
            result.setText("Walang permission. Payagan ang Phone at Location permissions sa App info.");
        } catch (Exception e) {
            result.setText("Hindi available ang cell information sa device na ito. Detalye: " + e.getClass().getSimpleName());
        }
    }
    private String validInt(int v) { return v < 0 ? "Hindi available" : String.valueOf(v); }
    private String validLong(long v) { return v < 0 ? "Hindi available" : String.valueOf(v); }
    private String safe(String s) { return s == null ? "?" : s; }
}
