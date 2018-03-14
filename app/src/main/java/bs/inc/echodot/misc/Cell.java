package bs.inc.echodot.misc;

/**
 * Created by shravan on 14/3/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Cell {

    @SerializedName("lac")
    @Expose
    private Integer lac;
    @SerializedName("cid")
    @Expose
    private Integer cid;
    @SerializedName("psc")
    @Expose
    private Integer psc;

    public Integer getLac() {
        return lac;
    }

    public void setLac(Integer lac) {
        this.lac = lac;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getPsc() {
        return psc;
    }

    public void setPsc(Integer psc) {
        this.psc = psc;
    }

}
