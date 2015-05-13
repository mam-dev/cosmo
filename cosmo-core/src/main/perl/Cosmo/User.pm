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

package Cosmo::User;

use Cosmo::Constants ();
use XML::DOM ();

use strict;

sub new {
    my $class = shift;

    my $self = {};
    $self->{username} = undef;
    $self->{password} = undef;
    $self->{first_name} = undef;
    $self->{last_name} = undef;
    $self->{email} = shift;
    $self->{user_url} = shift;
    $self->{homedir_url} = shift;

    return bless $self, $class;
}

sub username {
    my $self = shift;
    $self->{username} = shift if @_;
    return $self->{username};
}

sub password {
    my $self = shift;
    $self->{password} = shift if @_;
    return $self->{password};
}

sub first_name {
    my $self = shift;
    $self->{first_name} = shift if @_;
    return $self->{first_name};
}

sub last_name {
    my $self = shift;
    $self->{last_name} = shift if @_;
    return $self->{last_name};
}

sub email {
    my $self = shift;
    $self->{email} = shift if @_;
    return $self->{email};
}

sub user_url {
    my $self = shift;
    $self->{user_url} = shift if @_;
    return $self->{user_url};
}

sub homedir_url {
    my $self = shift;
    $self->{homedir_url} = shift if @_;
    return $self->{homedir_url};
}

sub to_xml {
    my $self = shift;

    my $username = $self->username();
    my $password = $self->password();
    my $first_name = $self->first_name();
    my $last_name = $self->last_name();
    my $email = $self->email();

    return <<EOT;
<?xml version="1.0" encoding="utf-8" ?>
<user xmlns="http://osafoundation.org/cosmo/CMP">
  <username>$username</username>
  <password>$password</password>
  <firstName>$first_name</firstName>
  <lastName>$last_name</lastName>
  <email>$email</email>
</user>
EOT
}

sub from_xml {
    my $class = shift;
    my $xml = shift;
    my $user = Cosmo::User->new();

    my $parser = XML::DOM::Parser->new();
    my $dom = $parser->parse($xml);
    for my $node ($dom->getDocumentElement()->getChildNodes()) {
        if ($node->getNodeName() eq 'username') {
            $user->username($node->getFirstChild()->getNodeValue());
            next;
        }
        if ($node->getNodeName() eq 'firstName') {
            $user->first_name($node->getFirstChild()->getNodeValue());
            next;
        }
        if ($node->getNodeName() eq 'lastName') {
            $user->last_name($node->getFirstChild()->getNodeValue());
            next;
        }
        if ($node->getNodeName() eq 'email') {
            $user->email($node->getFirstChild()->getNodeValue());
            next;
        }
        if ($node->getNodeName() eq 'url') {
            $user->user_url($node->getFirstChild()->getNodeValue());
            next;
        }
        if ($node->getNodeName() eq 'homedirUrl') {
            $user->homedir_url($node->getFirstChild()->getNodeValue());
            next;
        }
    }

    return $user;
}

1;
