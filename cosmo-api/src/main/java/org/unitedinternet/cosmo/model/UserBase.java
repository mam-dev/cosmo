package org.unitedinternet.cosmo.model;

import java.util.Set;

public interface UserBase  extends AuditableObject {
     /**
     */
    public String getUid();

    /**
     * @param uid
     */
    public void setUid(String uid);

    /**
     */
    public String getUsername();

    /**
     */
    public void setUsername(String username);

    /**
     */
    public String getOldUsername();

    /**
     */
    public boolean isUsernameChanged();

    public boolean isOverlord();





    public Boolean getAdmin();



    public Boolean getOldAdmin();

    /**
     */
    public boolean isAdminChanged();

    /**
     */
    public void setAdmin(Boolean admin);


    public Set<Preference> getPreferences();

    public void addPreference(Preference preference);

    public Preference getPreference(String key);

    public void removePreference(String key);

    public void removePreference(Preference preference);

    public String calculateEntityTag();

    public Set<CollectionSubscription> getSubscriptions();

        /**
     * Indicates whether the user is a member of given group.
     * @param group
     * @return
     */
    public boolean isMemberOf(Group group);
}
