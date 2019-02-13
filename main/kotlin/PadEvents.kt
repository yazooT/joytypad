package joytypad

enum class PadEvents(val text: String) {
    CENTER("center"),
    UP("up"),
    UP_RIGHT("up-right"),
    UP_LEFT("up-left"),
    DOWN("down"),
    DOWN_RIGHT("down-right"),
    DOWN_LEFT("down-left"),
    RIGHT("right"),
    LEFT("left"),
    PRESS("8"),

    A("0"),
    B("1"),
    X("2"),
    Y("3"),
    LB("4"),
    RB("5"),
    START("6"),
    BACK("7"),
//    L_PRESS("8"),
    R_PRESS("9"),
    LT("10"),
    RT("11"),
    POWER("12"),

    POV_UP("pov-up"),
    POV_DOWN("pov-down"),
    POV_RIGHT("pov-right"),
    POV_LEFT("pov-left")
}