package com.horzits.common.utils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 时间工具类
 * 
 * @author ruoyi
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", 
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     * 
     * @return Date() 当前日期
     */
    public static Date getNowDate()
    {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     * 
     * @return String
     */
    public static String getDate()
    {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime()
    {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow()
    {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format)
    {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date)
    {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date)
    {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts)
    {
        try
        {
            return new SimpleDateFormat(format).parse(ts);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str)
    {
        if (str == null)
        {
            return null;
        }
        try
        {
            return parseDate(str.toString(), parsePatterns);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate()
    {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2)
    {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算时间差
     *
     * @param endDate 最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor)
    {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor)
    {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }


    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    //获取当前时间前X天时间
    public static Date getBackOneDay(String length){
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, -Integer.valueOf(length));
        Date endDate = calendar.getTime();
        return endDate;
    }

    //获取之后指定X天时间
    public static String getAfterDate(String nowDate,String formateStr,String length){
        Date date = null;
        try {
            date = new SimpleDateFormat(formateStr).parse(nowDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, +Integer.valueOf(length));
        Date endDate = calendar.getTime();
        return new SimpleDateFormat(formateStr).format(endDate);
    }

    //格式化时间(Date格式)
    public static String formateDate(Date date,String formateStr){
        SimpleDateFormat format = new SimpleDateFormat(formateStr);
        return format.format(date);
    }

    //获取间隔时间长度(30天,间隔五天取数)
    public static List<Date> getTimeArea(Date startDate){
        List<Date> resultDate = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);
        for(int i=0;i<30;i=i+5){
            calendar.add(calendar.DATE, i);
            resultDate.add(calendar.getTime());
        }
        return resultDate;
    }

    //获取间隔时间长度（30天，间隔时间为1）
    public static List<String> getTimeAreaByOne(Date startDate){
        List<String> resultDate = new ArrayList<String>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);
        for(int i=0;i<30;i++){
            calendar.add(calendar.DATE, i);
            resultDate.add(formateDate(calendar.getTime(),"yyyyMMdd"));
        }
        return resultDate;
    }

    private static SimpleDateFormat getSDFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src
     *            将要转换的原始字符窜
     * @param pattern
     *            转换的匹配格式
     * @return 如果转换成功则返回转换后的日期
     * @throws ParseException
     */
    public static Date parseDate(String src, String pattern)
            throws ParseException {
        return getSDFormat(pattern).parse(src);

    }

    /**
     * 获取当前系统时间最近12月的年月（含当月）
     */
    public static String getLatest12Month(Date date){
        String ret="";
        Calendar  from  =  Calendar.getInstance();
        from.setTime(date);
        String str1 = from.get(Calendar.YEAR)+"-"+fillZero(from.get(Calendar.MONTH)-1);
        for(int i=1;i<=11;i++){
            if (i == 1) {
                from.add(Calendar.MONTH, 0);//11个月前
            }else {
                from.add(Calendar.MONTH, -1);//11个月前
            }
            if(i==11){
                ret +=from.get(Calendar.YEAR) + "-" + fillZero(from.get(Calendar.MONTH));
            }else {
                ret += from.get(Calendar.YEAR) + "-" + fillZero(from.get(Calendar.MONTH))+",";
            }
        }
        return ret;
    }


    /**
     * 格式化月份
     */
    public static String fillZero(int i){
        String month = "";
        if(i==0){
            month= "12";
        }else if(i<10){
            month = "0" + i;
        }else{
            month = String.valueOf(i);
        }
        return month;
    }

    public static List<String> getLast12Month(Date date,int months){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMM");
        List<String> dateList=new ArrayList<String>();
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH));
        for(int i=0;i<months;i++){
            calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-1);
            date = calendar.getTime();
            String accDate = sdf.format(date);
            dateList.add(accDate);
        }
        return dateList;
    }

    public static void main(String[] args) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            /*String days="10";

            String enddate = DateUtils.getAfterDate(sdf.format(new Date()), "yyyyMMdd", "-1");
            String startdate = DateUtils.getAfterDate(sdf.format(new Date()), "yyyyMMdd", "-"+days);
            String enddate1 = DateUtils.getAfterDate(startdate, "yyyyMMdd", "-1");
            String startdate1 = DateUtils.getAfterDate(startdate, "yyyyMMdd", "-"+days);
            String enddate2 = DateUtils.getAfterDate(startdate1, "yyyyMMdd", "-1");
            String startdate2 = DateUtils.getAfterDate(startdate1, "yyyyMMdd", "-"+days);

            System.out.println(enddate);
            System.out.println(startdate);
            System.out.println(enddate1);
            System.out.println(startdate1);
            System.out.println(enddate2);
            System.out.println(startdate2);
*/
//本期月份
            /*String nowDate[]=new String[12];
            List<String> last12Month = getLast12Month(new Date(), 12);
            for(int i=0;i<last12Month.size();i++){
                nowDate[i]=last12Month.get(last12Month.size()-1-i);
            }
            System.out.println(nowDate);*/

            System.out.println(lastDayForMonth(sdf.parse("20220401")));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String lastDayForMonth (Date date){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String format = sdf.format(calendar.getTime());
        return format;
    }

    public static List<String> getLast12MonthNew(int months){
        List<String> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, months);
        for (int i = 0; i < 12; i++) {
            Date date = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            String datestr = sdf.format(date);
            dateList.add(datestr);
            calendar.add(Calendar.MONTH, 1);
        }
        return dateList;
    }


}
