#!/usr/bin/perl -w
# -*- Mode: Perl; indent-tabs-mode: nil; -*-
# 
# Copyright 2005-2006 Open Source Applications Foundation
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

package Cosmo::DAV;

use Cosmo::Constants ();
use HTTP::DAV ();
use HTTP::Request ();

use strict;

sub new {
    my $class = shift;

    my $self = {};
    $self->{server_url} = shift;
    $self->{username} = shift;
    $self->{password} = shift;
    $self->{debug} = shift;
    $self->{dav} = HTTP::DAV->new();
    $self->{dav}->credentials(-realm => Cosmo::Constants::REALM,
                              -user => $self->{username},
                              -pass => $self->{password});
    $self->{dav}->DebugLevel($self->{debug} || 0);

    return bless $self, $class;
}

sub server_url {
    my $self = shift;
    return $self->{server_url};
}

sub repository_url {
    my $self = shift;
    return sprintf("%s%s/", $self->server_url,
                   Cosmo::Constants::URL_NS_REPOSITORY);
}

sub homedir_url {
    my $self = shift;
    return sprintf("%s%s/", $self->repository_url, $self->{username});
}

sub dav {
    my $self = shift;
    return $self->{dav};
}

sub agent {
    my $self = shift;
    my $id = shift;
    $self->dav()->get_user_agent()->agent($id);
}

sub check_server_availability {
    my $self = shift;
    $self->dav()->open(-url => $self->homedir_url) or
        die $self->dav()->message() . "\n";
}

sub mkcalendar {
    my $self = shift;
    my $url = shift;

    my $req = HTTP::Request->new(MKCALENDAR => $url);
    warn $req->as_string . "\n" if $self->{debug};

    my $res = $self->dav()->get_user_agent()->request($req);
    warn $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success()) {
        if ($res->code == 401) {
            die "Bad username or password\n";
        }
        die $res->status_line . "\n";
    }
}

1;
