package com.example.mdo3.vinylplayer;

/**
 * Created by mdo3 on 4/6/2018.
 */

import java.lang.Math.*;

public class FormulaClass
{
    final private String units = "in";
    final private double armLength = 7.75;
    final private double spacing = .012; //inches
    final private double offset = 30.0; //degrees
    final private double stepAngle = .005859375; //degrees

    private double rpm = 33.3;
    private double startTime = 160.5; //seconds

    private double angle = 0.0;
    private int steps = 0;

    public int getValue()
    {
        return calcValue();
    }

    private int calcValue()
    {

        double x = Math.pow((spacing * (rpm/60) * startTime),2);
        double y = 2 * Math.pow(armLength,2);

        double z = (x-y) / -y;
       angle = (180/Math.PI) * Math.acos(z) + offset;
       // angle =  offset;
        steps =(int) Math.ceil(angle/stepAngle);

        System.out.println("DEBUG: x " + x);
        System.out.println("DEBUG: y " + y);
        System.out.println("DEBUG: z " + z);
        System.out.println("DEBUG: angle " + angle);
        System.out.println("DEBUG: steps " + steps);
        return steps + 20000;
    }
}
