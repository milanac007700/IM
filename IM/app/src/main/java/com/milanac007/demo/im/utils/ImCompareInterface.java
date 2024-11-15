
package com.milanac007.demo.im.utils;

public interface ImCompareInterface {
    /**
     * @return return 0 if o1 = o2, return >0 if o1 after o2, return <0 if o1
     *         before o2
     */
    public int compare(Object o1, Object o2);
    public int getDataCount();
    public Object getData(int index);
}
