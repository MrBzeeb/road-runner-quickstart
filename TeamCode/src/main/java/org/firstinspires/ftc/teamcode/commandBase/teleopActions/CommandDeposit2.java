package org.firstinspires.ftc.teamcode.commandBase.teleopActions;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.commandBase.teleopAction;
import org.firstinspires.ftc.teamcode.gamepadEnhancements.ButtonPress;
import org.firstinspires.ftc.teamcode.subsystems.Robot;
import org.firstinspires.ftc.teamcode.subsystems.scoringMechanisms.Deposit;

import static org.firstinspires.ftc.teamcode.subsystems.scoringMechanisms.Deposit.depositStates.COLLECTION;
import static org.firstinspires.ftc.teamcode.subsystems.scoringMechanisms.Deposit.depositStates.GOING_IN;
import static org.firstinspires.ftc.teamcode.subsystems.scoringMechanisms.Deposit.depositStates.IN;

public class CommandDeposit2 implements teleopAction {

	Robot robot;

	Gamepad gamepad1;
	Gamepad gamepad2;

	public static final double DEPOSIT_DURATION = 370.0;
	public static final double GOING_IN_DURATION = 1000;

	protected Deposit.depositStates desiredUpState = Deposit.depositStates.AT_HIGH;
	protected Deposit.depositStates desiredUpStateTransition = Deposit.depositStates.GOING_TO_HIGH;
	protected Deposit.depositStates state = IN;

	ElapsedTime timer = new ElapsedTime();

	ButtonPress slideToggle = new ButtonPress();

	protected ButtonPress intakeButton = new ButtonPress();



	public CommandDeposit2(Robot robot, Gamepad gamepad1, Gamepad gamepad2) {
		this.robot = robot;
		this.gamepad1 = gamepad1;
		this.gamepad2 = gamepad2;
	}

	@Override
	public void initialRun() {

	}

	@Override
	public void periodic() {

		slideToggle.button(slideToggle());

		setDesiredStates();

		switch (state) {

			case DISARMED:
			case COLLECTION:
				break;
			case IN:
				if (slideToggle.press()) {
					timer.reset();
					state = desiredUpStateTransition;
				}
				break;
			case GOING_TO_HIGH:
			case GOING_TO_MID:
			case GOING_TO_LOW:
				if (timer.milliseconds() > DEPOSIT_DURATION) {
					state = desiredUpState;
				}
				break;
			case AT_HIGH:
			case AT_LOW:
			case AT_MID:
				transitionToDeposit();
				break;
			case DEPOSITING:
				if (gamepad1.right_bumper) {
					state = GOING_IN;
					timer.reset();
				}
				break;
			case GOING_IN:
				if (timer.milliseconds() > GOING_IN_DURATION) {
					state = IN;
					timer.reset();
				}
				break;
		}

		robot.Deposit.setState(state);
		robot.bucketSys.setState(state);

	}

	protected void transitionToDeposit() {
		if (activeDepositButton()) {
			state = Deposit.depositStates.DEPOSITING;
			timer.reset();
		}
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public boolean shouldRun() {
		boolean intakeButtonState = gamepad1.right_trigger > 0.5 || gamepad1.left_trigger > 0.5;
		intakeButton.button(intakeButtonState);

		if (intakeButtonState) {
			state = COLLECTION;
		} else if (intakeButton.release()) {
			state = IN;
		}



		return true;
	}

	@Override
	public void reset() {

	}

	@Override
	public boolean hasPerformedInitialRun() {
		return true;
	}


	private void setDesiredStates() {
		if (highButton()) {
			desiredUpState = Deposit.depositStates.AT_HIGH;
			desiredUpStateTransition = Deposit.depositStates.GOING_TO_HIGH;
		}
		if (midButton()) {
			desiredUpState = Deposit.depositStates.AT_MID;
			desiredUpStateTransition = Deposit.depositStates.GOING_TO_MID;
		}
		if (lowButton()) {
			desiredUpState = Deposit.depositStates.AT_LOW;
			desiredUpStateTransition = Deposit.depositStates.GOING_TO_LOW;
		}
	}


	public boolean highButton() {
		return gamepad1.triangle;
	}

	public boolean midButton() {
		return gamepad1.square;
	}

	public boolean lowButton() {
		return gamepad1.cross;
	}

	public boolean activeDepositButton() {
		return gamepad1.left_bumper;
	}

	public boolean slideToggle() {
		return gamepad1.right_bumper;
	}


}
