package org.tgramwear;

import java.io.Serializable;

/**
 * Created by dvlper.android on 15/05/2015.
 */

public class ContactE implements Comparable<ContactE>, Serializable {
    private String Number = "";
    private String Name = "";
    private long Id;
    private int IntId;

    public ContactE(long id, String name, String number, int intId){
        this.Number = number;
        this.Name = name;
        this.Id = id;
        this.IntId = intId;
    }

    public String getNumber(){return this.Number;}
    public String getName(){return this.Name;}
    public long getId(){return this.Id;}
    public int getIntId(){return this.IntId;}

    @Override
    public int compareTo(ContactE ce){
        int compare = 0;
        if (this.Name.compareTo(ce.getName()) == 0)
            compare = 0;
        else
        if (this.Name.compareTo(ce.getName()) < 0)
            compare = -1;
        else
        if (this.Name.compareTo(ce.getName()) > 0)
            compare = 1;

        return compare;
    }
}
