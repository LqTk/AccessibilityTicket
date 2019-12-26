package com.org.tickes;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.org.tickes.entities.PersonEntity;
import com.org.tickes.entities.TrainEntity;
import com.org.tickes.eventbus.NotifyData;
import com.org.tickes.utils.Global;
import com.org.tickes.utils.MyPreference;
import com.org.tickes.utils.Notifier;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.org.tickes.utils.MyPreference.personInfo;
import static com.org.tickes.utils.MyPreference.trainInfo;

public class TicketsService extends AccessibilityService {

    private List<TrainEntity> trainEntityList=new ArrayList<>();
    private List<PersonEntity> personEntityList=new ArrayList<>();
    private List<String> trainNumList = new ArrayList<>();
    private List<String> personNameList = new ArrayList<>();
    private boolean isTrainList = false;
    private boolean isTrainDetail = false;
    private boolean isBuyTickets = false;
    private List<String> getNowPassengerNames = new ArrayList<>();
    private boolean hasPassenger = false;

    @Override
    protected void onServiceConnected() {
        EventBus.getDefault().register(this);
        AccessibilityServiceInfo accessibilityServiceInfo = getServiceInfo();
        if (accessibilityServiceInfo == null)
            accessibilityServiceInfo = new AccessibilityServiceInfo();
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        accessibilityServiceInfo.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        accessibilityServiceInfo.packageNames = new String[] {Global.flyPig};
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        accessibilityServiceInfo.notificationTimeout = 10;
        setServiceInfo(accessibilityServiceInfo);
        // 4.0之后可通过xml进行配置,以下加入到Service里面
        /*
         * <meta-data android:name="android.accessibilityservice"
         * android:resource="@xml/accessibility" />
         */
        Notifier.getInstance().notify(getString(R.string.app_name), "抢票系统正在进行", "已启动",
                Notifier.SERVICE_RUNNING, false);
        myPreference = MyPreference.getInstance();
        myPreference.setPreference(getBaseContext());
        refreshData(new NotifyData());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence currentClassName = event.getClassName();
        Log.d("currentClassName:", (String) currentClassName);
        if (currentClassName.equals("com.taobao.trip.train.ui.list.TrainListActivity")){
            //查询车次页面
            isTrainList = true;
            isTrainDetail = false;
            isBuyTickets = false;
        }else if (currentClassName.equals("com.taobao.trip.train.traindetail.TrainDetailPageActivity")){
            isTrainDetail = true;
            isTrainList = false;
            isBuyTickets = false;
        }else if (currentClassName.equals("com.taobao.trip.fliggybuy.buynew.FliggyBuyNewActivity")){
            isBuyTickets = true;
            isTrainDetail = false;
            isTrainList = false;
        }else if (currentClassName.equals("com.taobao.trip.train.ui.passengerlist.TrainPassengerListActivityNew")
                || currentClassName.equals("com.taobao.android.purchase.core.view.widget.Container")){
            isBuyTickets = false;
            isTrainDetail = false;
            isTrainList = false;
            getPassenger();
        }else if (currentClassName.equals("com.taobao.trip.fliggybuy.buynew.basic.dialog.FliggyBuyInsuranceDialog")){
            isBuyTickets = false;
            isTrainDetail = false;
            isTrainList = false;
            createTicketOrder();
        }

        if (isTrainList){
            getTecketsContent();
        }else if (isTrainDetail){
            preOrderTicket();
        }else if (isBuyTickets){
            orderBuyTicket();
        }
    }

    private void createTicketOrder() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        List<AccessibilityNodeInfo> noBuyNode = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/fliggybuy_dialog_left_btn");
        if (noBuyNode!=null && noBuyNode.size()>0){
            noBuyNode.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    //选择乘客
    private void getPassenger() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        List<AccessibilityNodeInfo> tripNames = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/trip_tv_name");
        if (tripNames!=null && tripNames.size()>0){
            if (hasPassenger) {
                for (AccessibilityNodeInfo tripItem : tripNames) {
                    if (getNowPassengerNames.contains(tripItem.getText().toString().trim())) {
                        tripItem.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            for (AccessibilityNodeInfo tripItem:tripNames){
                if (personNameList.contains(tripItem.getText().toString().trim())){
                    tripItem.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            List<AccessibilityNodeInfo> btnFinish = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/btn_finish");
            if (btnFinish!=null && btnFinish.size()>0){
                btnFinish.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    //预定生成订单页面
    private void orderBuyTicket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        List<AccessibilityNodeInfo> passengerNames = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_passenger_name");
        if (passengerNames!=null && passengerNames.size()>0){
            getNowPassengerNames.clear();
            if (passengerNames.size()==personNameList.size()){
                boolean hasSetPassenger = true;
                for (int m=0;m<passengerNames.size();m++){
                    AccessibilityNodeInfo nameItem = passengerNames.get(m);
                    if (nameItem!=null) {
                        if (!TextUtils.isEmpty(nameItem.getText().toString())) {
                            if (!personNameList.contains(nameItem.getText().toString().trim())) {
                                hasSetPassenger = false;
                            }
                        }else {
                            hasSetPassenger = false;
                        }
                    }
                    getNowPassengerNames.add(nameItem.getText().toString().trim());
                }
                if (hasSetPassenger){
                    //乘车人正确
                    createOrder();
                }else {
                    //乘车人不正确
                    gotoSelectPassenger();
                }
            }else {
                for (int m=0;m<passengerNames.size();m++){
                    AccessibilityNodeInfo nameItem = passengerNames.get(m);
                    if (nameItem!=null) {
                        if (!TextUtils.isEmpty(nameItem.getText().toString())) {
                            getNowPassengerNames.add(nameItem.getText().toString().trim());
                        }
                    }
                }
                gotoSelectPassenger();
            }
        }else{
            gotoSelectPassenger();
        }
    }

    //立即预定
    private void createOrder() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        List<AccessibilityNodeInfo> orderImmediately = rootNode.findAccessibilityNodeInfosByText("立即预订");
        if (orderImmediately!=null && orderImmediately.size()>0){
            orderImmediately.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    //添加乘车人
    private void gotoSelectPassenger(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        /*List<AccessibilityNodeInfo> deletePassenger = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_remove_select_psg");
        if (deletePassenger!=null && deletePassenger.size()>0) {
            for (AccessibilityNodeInfo delete : deletePassenger) {
                delete.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }*/
        List<AccessibilityNodeInfo> addPassenger = rootNode.findAccessibilityNodeInfosByText("添加乘车人");
        if (addPassenger != null && addPassenger.size() > 0) {
            hasPassenger = false;
            List<AccessibilityNodeInfo> hasPassengerNode = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_have_psg_add_passenger");
            if (hasPassengerNode != null && hasPassengerNode.size() > 0) {
                hasPassenger = true;
            }
            addPassenger.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }

    //预定页面
    private void preOrderTicket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        List<AccessibilityNodeInfo> trainNumberNode = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/table_train_number");
        if (trainNumberNode!=null && trainNumberNode.size()>0){
            String text = trainNumberNode.get(0).getText().toString();
            if (trainNumList.contains(text)) {
                List<String> setTrainSeat = trainEntityList.get(trainNumList.lastIndexOf(text)).getSeatNameList();
                List<AccessibilityNodeInfo> seatsContainer = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_no_seats_container");
                if (seatsContainer!=null && seatsContainer.size()>0) {
                    List<AccessibilityNodeInfo> detailSeatName = seatsContainer.get(0).findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_detail_seat_name");
                    List<AccessibilityNodeInfo> detailStockInfo = seatsContainer.get(0).findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_detail_stock_info");
                    List<AccessibilityNodeInfo> detailOrderBtn = seatsContainer.get(0).findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_detail_btn");
                    for (int k = 0; k < setTrainSeat.size(); k++) {
                        String setName = setTrainSeat.get(k);
                        boolean breakok = false;
                        for (int i=0;i<detailSeatName.size();i++){
                            String recentSeatName = detailSeatName.get(i).getChild(0).getText().toString();
                            if (recentSeatName.equals(setName)){
                                String stockText = detailStockInfo.get(i).getText().toString();
                                if (stockText.equals("有票")){
                                    String orderText = detailOrderBtn.get(i).getText().toString();
                                    if (orderText.equals("立即预订")){
                                        detailOrderBtn.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        breakok = true;
                                        break;
                                    }
                                }else if (stockText.contains("仅剩")){
                                    String ticketCount = stockText.substring(2, stockText.indexOf("张"));
                                    try {
                                        int nowCountTicket = Integer.valueOf(ticketCount);
                                        if (nowCountTicket>=personNameList.size()){
                                            String orderText = detailOrderBtn.get(i).getText().toString();
                                            if (orderText.equals("立即预订")){
                                                detailOrderBtn.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                breakok = true;
                                                break;
                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (breakok)
                            break;
                    }
                }
            }
        }
    }

    //列车列表
    private void getTecketsContent() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode==null)
            return;
        List<AccessibilityNodeInfo> trainNumberNode = rootNode.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_item_number");
        if (trainNumberNode!=null && trainNumberNode.size()>0){
            for (AccessibilityNodeInfo numBer:trainNumberNode){
                String text = numBer.getText().toString().trim();
                if (trainNumList.contains(text)) {
                    List<String> nowTrainSeat = trainEntityList.get(trainNumList.lastIndexOf(text)).getSeatNameList();
                    AccessibilityNodeInfo numParent = numBer.getParent();
                    List<AccessibilityNodeInfo> seatLineNode = numParent.findAccessibilityNodeInfosByViewId("com.taobao.trip:id/train_item_seat_line");
                    if (seatLineNode != null && seatLineNode.size() > 0) {
                        AccessibilityNodeInfo setLine = seatLineNode.get(0);
                        for (int m=0;m<nowTrainSeat.size();m++){
                            String currentSetSeatName = nowTrainSeat.get(m);
                            boolean breakOk = false;
                            for (int i = 0; i < setLine.getChildCount(); i++) {
                                AccessibilityNodeInfo child = setLine.getChild(i);
                                if (child!=null && !TextUtils.isEmpty(child.getText())) {
                                    String seatName = child.getText().toString();
                                    if (seatName.contains(currentSetSeatName)) {
                                        if (seatName.contains("有票")) {
                                            child.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                            breakOk = true;
                                            break;
                                        }else if (seatName.contains("张")){
                                            String ticketCount = seatName.substring(currentSetSeatName.length()+1, seatName.indexOf("张"));
                                            try {
                                                int nowCountTicket = Integer.valueOf(ticketCount);
                                                if (nowCountTicket>=personNameList.size()) {
                                                    child.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                    breakOk = true;
                                                    break;
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    Log.d("seatName", text + child.getText().toString());
                                }
                            }
                            if (breakOk){
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onInterrupt() {

    }

    MyPreference myPreference;
    @Subscribe
    public void refreshData(NotifyData notifyData){
        if (myPreference==null){
            myPreference = MyPreference.getInstance();
            myPreference.setPreference(getBaseContext());
        }
        if (myPreference==null){
            return;
        }
        personEntityList = myPreference.getListObject(personInfo, PersonEntity.class);
        trainEntityList = myPreference.getListObject(trainInfo, TrainEntity.class);
        if (personEntityList==null){
            personEntityList = new ArrayList<>();
        }
        if (trainEntityList==null){
            trainEntityList = new ArrayList<>();
        }

        trainNumList.clear();
        for (TrainEntity entity:trainEntityList){
            trainNumList.add(entity.getTrainNumber());
        }
        personNameList.clear();
        for (PersonEntity entity:personEntityList){
            personNameList.add(entity.getName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
