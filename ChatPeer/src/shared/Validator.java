package shared;

public class Validator {
    public static boolean isRoomIdValid(String roomid){
        boolean matchResult = roomid.matches("[A-Za-z0-9]+");
        boolean firstLetter = Character.isLetter(roomid.charAt(0));
        boolean lengthCheckResult = roomid.length() >= 3 && roomid.length() <= 32;
        return matchResult && firstLetter && lengthCheckResult;
    }
}
