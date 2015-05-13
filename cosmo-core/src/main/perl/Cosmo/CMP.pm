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

package Cosmo::CMP;

use Cosmo::Constants ();
use Cosmo::User ();

use strict;
use base qw(LWP::UserAgent);

sub new {
    my $class = shift;

    my $self = LWP::UserAgent->new();
    $self->{server_url} = shift;
    $self->{username} = shift;
    $self->{password} = shift;
    $self->{debug} = shift;

    return bless $self, $class;
}

sub server_url {
    my $self = shift;
    return $self->{server_url};
}

sub user_url {
    my $self = shift;
    my $username = shift;

    return sprintf("%s%s/%s", $self->server_url,
                   Cosmo::Constants::URL_NS_CMP_USER, $username);
}

sub homedir_url {
    my $self = shift;
    my $username = shift;

    return sprintf("%s%s/%s/", $self->server_url,
                   Cosmo::Constants::URL_NS_REPOSITORY, $username);
}

sub get_basic_credentials {
    my $self = shift;
    my $realm = shift;
    my $uri = shift;
    my $isproxy = shift;

    if ($realm eq Cosmo::Constants::REALM) {
        return ($self->{username}, $self->{password});
    }
    return ();
}

sub check_server_availability {
    my $self = shift;
    my $res = $self->get($self->server_url);
    # GET / returns redirect to login page
    $res->is_redirect or
        die $res->status_line . "\n";
}

sub create_user {
    my $self = shift;
    my $user = shift;
    my $user_url = $self->user_url($user->username);

    my $req = HTTP::Request->new(PUT => $user_url);
    $req->content_type("text/xml; charset='utf-8'");
    $req->content($user->to_xml);
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        if ($res->code == 401) {
            die "Bad username or password\n";
        }
        die $res->status_line . "\n";
    }

    my $user2 = $self->get_user($user->username);
    $user->user_url($user2->user_url());
    $user->homedir_url($user2->homedir_url());

    return 1;
}

sub get_user {
    my $self = shift;
    my $username = shift;
    my $user_url = $self->user_url($username);

    my $req = HTTP::Request->new(GET => $user_url);
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        if ($res->code == 401) {
            die "Bad username or password\n";
        }
        die $res->status_line . "\n";
    }

    return Cosmo::User->from_xml($res->content);
}

sub remove_user {
    my $self = shift;
    my $user = shift;
    my $user_url = $self->user_url($user->username);

    my $req = HTTP::Request->new(DELETE => $user_url);
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        if ($res->code == 401) {
            die "Bad username or password\n";
        }
        die $res->status_line . "\n";
    }

    return 1;
}

1;
