package pl.kskoczyk.todoandroid;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TODOtask implements Serializable {
    String name;
    String description;
    //transient SimpleDateFormat format = new SimpleDateFormat("dd-MM"); // import z Javy, Android wymaga API aż 24
    Date date;


    public TODOtask(String name, String description, Date date) {
        this.name = name;
        this.description = description;
        this.date = date;
    }

    // dateFormat.format(date));

    // TODO: rozdzielić datę i nazwę na dwa textfieldy?
    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM"); // nie ma go jako pola klasy, ponieważ były potworne problemy z serializacją
        return name + " " + format.format(date);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getStringDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(date);
    }
}
