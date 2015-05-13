#!/usr/bin/perl -w
# -*- Mode: Perl; indent-tabs-mode: nil; -*-
# 
# Copyright 2007 Open Source Applications Foundation
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

package Cosmo::ClientBase;

use Cosmo::Constants ();
use LWP::UserAgent ();

use strict;
use base qw(LWP::UserAgent);

use constant HEADER_TICKET => 'Ticket';

sub new {
    my $class = shift;

    my $self = LWP::UserAgent->new();
    $self->{server_url} = shift;
    $self->{ticket} = shift;
    $self->{username} = shift;
    $self->{password} = shift;
    $self->{debug} = shift;

    return bless $self, $class;
}

sub server_url {
    my $self = shift;
    return $self->{server_url};
}

sub get_basic_credentials {
    my $self = shift;
    my $realm = shift;
    my $uri = shift;
    my $isproxy = shift;

    return () if $self->{ticket};

    if ($realm eq Cosmo::Constants::REALM) {
        return ($self->{username}, $self->{password});
    }

    return ();
}

sub check_server_availability {
    my $self = shift;
    my $req = HTTP::Request->new("GET", $self->server_url);
    my $res = $self->simple_request($req);
    $res->is_redirect or
        die $res->status_line . "\n";
}

sub ticket {
    my $self = shift;
    return $self->{ticket};
}

sub username {
    my $self = shift;
    return $self->{username};
}

sub prepare_request {
    my $self = shift;
    my $request = shift;

    if ($self->{ticket}) {
        $request->header(HEADER_TICKET, $self->{ticket});
    }

    return $self->SUPER::prepare_request($request);
}

1;
