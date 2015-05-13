/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.service;

import java.util.Set;

import org.unitedinternet.cosmo.model.Schedule;
import org.unitedinternet.cosmo.model.User;

/**
 * Interface for services that manage user schedules
 */
public interface ScheduleService extends Service {
    
    public Set<User> getUsersWithSchedules();
    
    public Schedule createScheduleForUser(Schedule schedule, User user);
    
    public void deleteScheduleForUser(Schedule schedule, User user);
    
    public Schedule updateScheduleForUser(Schedule schedule, User user);

    public Set<Schedule> getSchedulesForUser(User user);

    public void enableScheduleForUser(Schedule schedule, User user, boolean enabled);  
}
