package com.org.tickes;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.org.tickes.adapter.AdapterInfo;
import com.org.tickes.adapter.AdapterPerson;
import com.org.tickes.entities.PersonEntity;
import com.org.tickes.entities.TrainEntity;
import com.org.tickes.eventbus.NotifyData;
import com.org.tickes.eventbus.StopServiceEvent;
import com.org.tickes.ui.BrokenRestart;
import com.org.tickes.utils.Global;
import com.org.tickes.utils.MyPreference;
import com.org.tickes.utils.PackageUtils;
import com.org.tickes.utils.ServiceHelper;
import com.org.tickes.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.org.tickes.utils.MyPreference.fileName;
import static com.org.tickes.utils.MyPreference.personInfo;
import static com.org.tickes.utils.MyPreference.trainInfo;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_service_statue)
    TextView tvServiceStatue;
    @BindView(R.id.tv_start_service)
    TextView tvStartService;
    @BindView(R.id.tv_open_flypig)
    TextView tvOpenFlypig;
    @BindView(R.id.rcv_info)
    RecyclerView rcvInfo;
    @BindView(R.id.tv_noinfo)
    TextView tvNoinfo;
    @BindView(R.id.rcv_person)
    RecyclerView rcvPerson;
    @BindView(R.id.tv_stop_service)
    TextView tvStopService;

    private Unbinder bind;
    private Context context;
    private AdapterInfo adapterInfo;
    private AdapterPerson adapterPerson;
    private MyPreference preference;
    private List<PersonEntity> personEntityList;
    private List<TrainEntity> trainEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bind = ButterKnife.bind(this);
        context = this;
        preference = MyPreference.getInstance();
        preference.setPreference(context);
        CathcExceptioin();
        refreshLayout();

        personEntityList = preference.getListObject(personInfo, PersonEntity.class);
        trainEntityList = preference.getListObject(trainInfo, TrainEntity.class);
        if (personEntityList == null) {
            personEntityList = new ArrayList<>();
        }
        if (trainEntityList == null) {
            trainEntityList = new ArrayList<>();
        }

        initRcv();
    }

    private void initRcv() {
        LinearLayoutManager personManager = new LinearLayoutManager(context);
        personManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvPerson.setLayoutManager(personManager);
        adapterPerson = new AdapterPerson(R.layout.rcv_info_item, personEntityList);
        rcvPerson.setAdapter(adapterPerson);
        adapterPerson.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                personEntityList.remove(position);
                preference.setObject(personInfo, personEntityList);
                adapterPerson.notifyDataSetChanged();
                EventBus.getDefault().post(new NotifyData());
            }
        });

        LinearLayoutManager trainManager = new LinearLayoutManager(context);
        trainManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvInfo.setLayoutManager(trainManager);
        adapterInfo = new AdapterInfo(R.layout.rcv_info_item, trainEntityList);
        rcvInfo.setAdapter(adapterInfo);
        adapterInfo.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                trainEntityList.remove(position);
                preference.setObject(trainInfo, trainEntityList);
                adapterInfo.notifyDataSetChanged();
                EventBus.getDefault().post(new NotifyData());
            }
        });
    }


    /**
     * 检测程序异常，重启
     */
    private void CathcExceptioin() {
        //添加程序异常检测
        BrokenRestart application = (BrokenRestart) getApplication();
        application.init();
        application.addActivity(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout();
    }

    private void refreshLayout() {
        boolean isOn = false;
        if (tvServiceStatue == null) {
            return;
        }
        if (context == null) {
            context = this;
        }
        isOn = ServiceHelper.getServiceStatue(context);

        if (isOn){
            tvStopService.setVisibility(View.VISIBLE);
            if (!Global.isStop) {
                tvServiceStatue.setText("服务已启动");
                tvServiceStatue.setTextColor(Color.BLUE);
                tvStopService.setText("暂停");
                tvStopService.setTextColor(Color.RED);
            }else {
                tvServiceStatue.setText("服务已暂停");
                tvServiceStatue.setTextColor(Color.RED);
                tvStopService.setText("继续");
                tvStopService.setTextColor(Color.BLUE);
            }
        }else {
            tvStopService.setVisibility(View.GONE);
            Global.isStop = false;
            tvStopService.setText("暂停");
            tvStopService.setTextColor(Color.RED);
            tvStopService.setVisibility(View.GONE);
            tvServiceStatue.setText("服务未启动");
            tvServiceStatue.setTextColor(Color.RED);
        }

        tvStartService.setText(isOn ? "停止服务" : "启动服务");
        tvStartService.setTextColor(isOn ? Color.RED : Color.WHITE);

        if (PackageUtils.isAppInstalled(context, Global.flyPig)) {
            tvOpenFlypig.setText("打开飞猪 APP");
            tvOpenFlypig.setBackgroundColor(getResources().getColor(R.color.colorGreen));
        } else {
            tvOpenFlypig.setText("飞猪App未安装");
            tvOpenFlypig.setBackgroundColor(Color.parseColor("#999999"));
        }

    }

    @OnClick({R.id.tv_start_service, R.id.tv_open_flypig, R.id.ll_addinfo, R.id.ll_addperson, R.id.tv_stop_service})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_start_service:
                if (context == null) {
                    context = MainActivity.this;
                }
                if (context == null) {
                    return;
                }
                checkPermission();
                break;
            case R.id.tv_open_flypig:
                PackageUtils.openApp(context, Global.flyPig);
                break;
            case R.id.ll_addinfo:
                addTrainInfoDialog();
                break;
            case R.id.ll_addperson:
                addPersonDialog();
                break;
            case R.id.tv_stop_service:
                if (!tvServiceStatue.getText().toString().equals("服务未启动")) {
                    if (Global.isStop) {
                        Global.isStop = false;
                        tvStopService.setText("暂停");
                        tvStopService.setTextColor(Color.RED);
                    } else {
                        Global.isStop = true;
                        tvStopService.setText("继续");
                        tvStopService.setTextColor(Color.BLUE);
                    }
                    if (tvServiceStatue.getText().toString().equals("服务已启动") && Global.isStop) {
                        tvServiceStatue.setText("服务已暂停");
                        tvServiceStatue.setTextColor(Color.RED);
                    } else if (tvServiceStatue.getText().toString().equals("服务已暂停") && !Global.isStop) {
                        tvServiceStatue.setText("服务已启动");
                        tvServiceStatue.setTextColor(Color.BLUE);
                    }
                }
                break;
        }
    }

    boolean isMan = true;

    private void addPersonDialog() {
        final String[] spinnerItems = {"男", "女"};
        LayoutInflater inflater = LayoutInflater.from(context);
        final View DialogView = inflater
                .inflate(R.layout.dialog_add_person, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(DialogView);
        AlertDialog alertDialog = builder.create();
        EditText editText = DialogView.findViewById(R.id.et_name);
        TextView tv_man = DialogView.findViewById(R.id.tv_man);
        TextView tv_woman = DialogView.findViewById(R.id.tv_woman);
        TextView tv_cancel = DialogView.findViewById(R.id.tv_cancel);
        TextView tv_commit = DialogView.findViewById(R.id.tv_commit);

        tv_man.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMan = true;
                tv_woman.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                tv_woman.setTextColor(getResources().getColor(R.color.textColor));
                tv_man.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                tv_man.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        });
        tv_woman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMan = false;
                tv_man.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                tv_man.setTextColor(getResources().getColor(R.color.textColor));
                tv_woman.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                tv_woman.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        tv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    ToastUtil.xtShort(context, "请输入姓名");
                    return;
                }
                PersonEntity personEntity = new PersonEntity();
                personEntity.setName(editText.getText().toString().trim());
                personEntity.setSex(isMan ? "男" : "女");
                personEntityList.add(personEntity);
                adapterPerson.notifyDataSetChanged();
                preference.setObject(personInfo, personEntityList);
                EventBus.getDefault().post(new NotifyData());
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    boolean c1 = false;
    boolean c2 = false;
    boolean c3 = false;
    boolean c4 = false;
    boolean c5 = false;
    boolean c6 = false;

    private void addTrainInfoDialog() {
        c1 = false;
        c2 = false;
        c3 = false;
        c4 = false;
        c5 = false;
        c6 = false;
        List<String> seats = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View DialogView = inflater
                .inflate(R.layout.train_info_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(DialogView);
        AlertDialog alertDialog = builder.create();
        EditText editText = DialogView.findViewById(R.id.et_train_name);
        TextView tv_cancel = DialogView.findViewById(R.id.tv_cancel);
        TextView tv_commit = DialogView.findViewById(R.id.tv_commit);
        TextView tv_seat1 = DialogView.findViewById(R.id.tv_seat1);
        TextView tv_seat2 = DialogView.findViewById(R.id.tv_seat2);
        TextView tv_seat3 = DialogView.findViewById(R.id.tv_seat3);
        TextView tv_seat4 = DialogView.findViewById(R.id.tv_seat4);
        TextView tv_seat5 = DialogView.findViewById(R.id.tv_seat5);
        TextView tv_seat6 = DialogView.findViewById(R.id.tv_seat6);

        tv_seat1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c1) {
                    c1 = false;
                    tv_seat1.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                    tv_seat1.setTextColor(getResources().getColor(R.color.textColor));
                    seats.remove(tv_seat1.getText().toString().trim());
                } else {
                    c1 = true;
                    tv_seat1.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                    tv_seat1.setTextColor(getResources().getColor(R.color.colorWhite));
                    seats.add(tv_seat1.getText().toString().trim());
                }
            }
        });
        tv_seat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c2) {
                    c2 = false;
                    tv_seat2.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                    tv_seat2.setTextColor(getResources().getColor(R.color.textColor));
                    seats.remove(tv_seat2.getText().toString().trim());
                } else {
                    c2 = true;
                    tv_seat2.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                    tv_seat2.setTextColor(getResources().getColor(R.color.colorWhite));
                    seats.add(tv_seat2.getText().toString().trim());
                }
            }
        });
        tv_seat3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c3) {
                    c3 = false;
                    tv_seat3.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                    tv_seat3.setTextColor(getResources().getColor(R.color.textColor));
                    seats.remove(tv_seat3.getText().toString().trim());
                } else {
                    c3 = true;
                    tv_seat3.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                    tv_seat3.setTextColor(getResources().getColor(R.color.colorWhite));
                    seats.add(tv_seat3.getText().toString().trim());
                }
            }
        });
        tv_seat4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c4) {
                    c4 = false;
                    tv_seat4.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                    tv_seat4.setTextColor(getResources().getColor(R.color.textColor));
                    seats.remove(tv_seat4.getText().toString().trim());
                } else {
                    c4 = true;
                    tv_seat4.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                    tv_seat4.setTextColor(getResources().getColor(R.color.colorWhite));
                    seats.add(tv_seat4.getText().toString().trim());
                }
            }
        });
        tv_seat5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c5) {
                    c5 = false;
                    tv_seat5.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                    tv_seat5.setTextColor(getResources().getColor(R.color.textColor));
                    seats.remove(tv_seat5.getText().toString().trim());
                } else {
                    c5 = true;
                    tv_seat5.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                    tv_seat5.setTextColor(getResources().getColor(R.color.colorWhite));
                    seats.add(tv_seat5.getText().toString().trim());
                }
            }
        });
        tv_seat6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c6) {
                    c6 = false;
                    tv_seat6.setBackground(getResources().getDrawable(R.drawable.seat_bg));
                    tv_seat6.setTextColor(getResources().getColor(R.color.textColor));
                    seats.remove(tv_seat6.getText().toString().trim());
                } else {
                    c6 = true;
                    tv_seat6.setBackground(getResources().getDrawable(R.drawable.seat_select_bg));
                    tv_seat6.setTextColor(getResources().getColor(R.color.colorWhite));
                    seats.add(tv_seat6.getText().toString().trim());
                }
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        tv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    ToastUtil.xtShort(context, "请输入列车号");
                    return;
                }
                TrainEntity trainEntity = new TrainEntity();
                trainEntity.setTrainNumber(editText.getText().toString());
                trainEntity.setSeatNameList(seats);
                trainEntityList.add(trainEntity);
                adapterInfo.notifyDataSetChanged();
                preference.setObject(trainInfo, trainEntityList);
                EventBus.getDefault().post(new NotifyData());
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @NeedsPermission(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
    public void checkPermission() {
        ServiceHelper.startService(context);
    }

    /**
     * 权限解释
     */
    @OnShowRationale(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
    public void showWhyNeed(final PermissionRequest request) {
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage("请求使用辅助权限")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.proceed();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.cancel();
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    /**
     * 用户取消拒绝权限
     */
    @OnPermissionDenied(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
    public void showDenied() {
        ToastUtil.xtShort(context, "获取权限失败");
    }

    /**
     * 勾选不再提示并且拒绝的回调
     */
    @OnNeverAskAgain(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
    public void showNeverAskAgain() {
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage("应用权限被拒绝,为了不影响您的正常使用，请在 权限 中开启对应权限")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    /**
     * 权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
    }
}
