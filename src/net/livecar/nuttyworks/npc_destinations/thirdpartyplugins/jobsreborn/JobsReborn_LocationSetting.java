package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.jobsreborn;

import java.util.UUID;

public class JobsReborn_LocationSetting {
    public UUID    locationID;
    public String  jobs_Name;    // Job name
    public boolean jobs_Greater; // False=Must have
                                 // less than Max, True
                                 // Must have max or
                                 // more
    public int     jobs_Max;     // -1 = max job slots,
                                 // 0=ignore, >0 are
                                 // max players in job.
}
