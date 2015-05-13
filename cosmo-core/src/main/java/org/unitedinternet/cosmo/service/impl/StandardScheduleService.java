/*
 * Copyright 2008 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.service.impl;

import java.util.Set;

import org.unitedinternet.cosmo.dao.ScheduleDao;
import org.unitedinternet.cosmo.model.Schedule;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.ScheduleService;

/**
 * Standard implementation of {@link ScheduleService}.
 */
public class StandardScheduleService extends BaseService implements ScheduleService {

    private ScheduleDao scheduleDao;
    
    public Schedule createScheduleForUser(Schedule schedule, User user) {
        return scheduleDao.createScheduleForUser(schedule, user);
    }

    public void deleteScheduleForUser(Schedule schedule, User user) {
        scheduleDao.deleteScheduleForUser(schedule, user);
    }

    public void enableScheduleForUser(Schedule schedule, User user,
            boolean enabled) {
        scheduleDao.enableScheduleForUser(schedule, user, enabled);
    }

    public Set<Schedule> getSchedulesForUser(User user) {
        return scheduleDao.getSchedulesForUser(user);
    }

    public Set<User> getUsersWithSchedules() {
        return scheduleDao.getUsersWithSchedules();
    }

    public Schedule updateScheduleForUser(Schedule schedule, User user) {
        return scheduleDao.updateScheduleForUser(schedule, user);
    }

    public void destroy() {
      
    }

    public void init() {
        if (scheduleDao == null) {
            throw new IllegalStateException("scheduleDao is required");
        }
    }

    public ScheduleDao getScheduleDao() {
        return scheduleDao;
    }

    public void setScheduleDao(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

   
}
