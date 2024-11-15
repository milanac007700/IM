
package com.milanac007.demo.im.utils;

public class ImExtBinarySearch {
    
    /**
     * 二分法搜索一个对象如果要插入到给定的有序数组中所应该排列的位置
     * 
     * @param data 有序数组，可以是升序或降序
     * @param item 想要插入的对象
     * @param comp 对象大小的比较方法
     * @return -1表示传入的数组为空；-2表示数组只有一个值，所以任何位置都可以；0或其他正数表示应该插入的位置
     */
    public int search(Object item, ImCompareInterface comp) {

        int isAscend = 1;

//        if (data == null || data.length == 0) {
        if(comp.getDataCount() == 0){
            //数组为空的时候返回-1
            return -1;
        } else if (comp.getDataCount() == 1) {
            //数组只有一个值，返回-2
            return -2;
        } else {
            if (comp.compare(comp.getData(0), comp.getData(comp.getDataCount() - 1)) > 0) {
                //第一个值大于最后一个值，数组是逆序的
                isAscend = -1;
            } else if (comp.compare(comp.getData(0), comp.getData(comp.getDataCount() - 1)) < 0) {
                //第一个值小于最后一个值，数组是正序的
                isAscend = 1;
            } else {
                //数组内的值都相同，返回0
                return 0;
            }
        }

        //二分法查找位置
        int start = 0;
        int end = comp.getDataCount() - 1;
        while (start <= end) {
            int searchIndex = (start + end) / 2;

            int r = comp.compare(item, comp.getData(searchIndex));
            if (r * isAscend > 0) {     //目标比中值大
                
                if (searchIndex == comp.getDataCount() - 1) {
                    return comp.getDataCount();
                }

                // 同序
                if (comp.compare(comp.getData(searchIndex + 1), item) * isAscend >= 0) {
                    return searchIndex + 1;
                } else {
                    start = searchIndex + 1;
                }
            } else if (r * isAscend < 0) {  //目标比中值小

                if (searchIndex == 0) {
                    return 0;
                }
                // 逆序
                if (comp.compare(item, comp.getData(searchIndex - 1)) * isAscend >= 0) {
                    return searchIndex;
                } else {
                    end = searchIndex - 1;
                }
            } else {
                // 相同
                return searchIndex;
            }
        }

        return 0;
    }
}
