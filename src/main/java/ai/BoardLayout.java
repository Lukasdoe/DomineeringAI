package ai;

import java.util.ArrayList;

/*
    A BoardLayout object contains all the calculated board areas for either the vertical or the horizontal player. It
     is only used for storing these objects and values.
 */

public class BoardLayout {
    public final ArrayList<ProtectiveArea> protectiveAreas;
    public final ArrayList<SafeArea> safeAreas;
    public final ArrayList<VulnArea> vulnAreasOne;
    public final ArrayList<VulnArea> vulnAreasTwo;
    public final ArrayList<VulnArea> vulnAreasProtectedOne;
    public final ArrayList<VulnArea> vulnAreasProtectedTwo;
    public final ArrayList<OptionArea> optionAreas;

    public final Player player;

    public int lowerBound;
    public int upperBound;

    public int unavailableSquares;
    public int startAvailableSquares;
    public int unplayableSquares;

    public BoardLayout(Player player) {
        this.player = player;
        this.protectiveAreas = new ArrayList<>(20);
        this.safeAreas = new ArrayList<>(15);
        this.vulnAreasTwo = new ArrayList<>(50);
        this.vulnAreasOne = new ArrayList<>(15);
        this.vulnAreasProtectedTwo = new ArrayList<>(10);
        this.vulnAreasProtectedOne = new ArrayList<>(10);
        this.optionAreas = new ArrayList<>(15);

        this.lowerBound = Integer.MIN_VALUE;
        this.upperBound = Integer.MIN_VALUE;
        this.unplayableSquares = Integer.MIN_VALUE;

        this.unavailableSquares = 0;
        this.startAvailableSquares = 0;
    }

    public int numProtectiveAreas() {
        return protectiveAreas.size();
    }

    public int numSafeAreas() {
        return safeAreas.size();
    }

    public int numVulnAreasOne() {
        return vulnAreasOne.size();
    }

    public int numVulnAreasTwo() {
        return vulnAreasTwo.size();
    }

    public int numVulnAreasProtectedOne() {
        return vulnAreasProtectedOne.size();
    }

    public int numVulnAreasProtectedTwo() {
        return vulnAreasProtectedTwo.size();
    }
}
