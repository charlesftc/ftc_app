package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Func;

import java.util.Locale;

import static java.lang.Double.NaN;

public class ArmCoordinator {
    private LinearOpMode opmode;
    private Gamepad gamepad;
    private Shoulder shoulder;
    private Slide slide;
    private ServoElbow elbow;

    private double foldPos[] = {88.3, 0, 3.5};
    private double preScoringPos1[] = {NaN, 0.1, 60};
    private double scoringPos1[] = {9, 0.91, 45};
    private double intakePos1[] = {};
    private double unfoldPos[] = {};

    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevUp = false;
    private boolean prevDown = false;
    private boolean prevLeft = false;
    private boolean prevRight = false;

    public ArmCoordinator(Teleop1 opmode, Gamepad gamepad, Shoulder shoulder1, Slide slide1, ServoElbow elbow1) {
        this.opmode = opmode;
        this.gamepad = gamepad;
        this.shoulder = shoulder1;
        this.elbow = elbow1;
        this.slide = slide1;

        /*opmode.telemetry.addLine().addData("Joints", new Func<String>() {
            @Override
            public String value() {
                return String.format(Locale.getDefault(), "shoulder %.2f, slide %.2f, elbow %.2f",
                        shoulder.getAngle(), slide.getExtension(), elbow.getAngle());
            }
        });*/
    }

    public void update() {
        if (prevX && !gamepad.x) {
            setGoals(preScoringPos1);
        }

        if (prevY && !gamepad.y) {
            setGoals(scoringPos1);
        }

        prevX = gamepad.x;
        prevY = gamepad.y;
    }

    private void setGoals(double goals[]) {
        shoulder.setGoal(goals[0]);
        slide.setGoal(goals[1]);
        elbow.setGoal(goals[2]);
    }
}
