package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.util.ElapsedTime;

public class DPSCalculator extends Thread {
    private boolean run = true;
    private double DPS;
    private double prevPos;
    private AnalogInput pot;
    private double waitTime;
    private ElapsedTime runtime = new ElapsedTime();

    public DPSCalculator(AnalogInput pot, double waitTime) {
        this.pot = pot;
        this.waitTime = waitTime;
    }

    public void run() {
        prevPos = pot.getVoltage() / 0.01222;

        while (run = true) {
            double pos = pot.getVoltage() / 0.01222;
            double deltaPos = pos - prevPos;
            DPS = deltaPos / waitTime;

            busySleep(waitTime * 1000);

            prevPos = pos;
        }
    }

    public void end() {
        run = false;
    }

    public double getDPS() {
        return DPS;
    }

    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }
}
