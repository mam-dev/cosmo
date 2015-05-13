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

package Cosmo::MC;

use Cosmo::ClientBase ();

use strict;
use base qw(Cosmo::ClientBase);

use constant HEADER_SYNC_TOKEN => 'X-MorseCode-SyncToken';
use constant HEADER_TICKET_TYPE => 'X-MorseCode-TicketType';
use constant HEADER_TICKET => 'X-MorseCode-Ticket';

use constant TICKET_TYPE_READ_ONLY => 'read-only';
use constant TICKET_TYPE_READ_WRITE => 'read-write';

sub new {
    my $class = shift;
    my $self = Cosmo::ClientBase->new(@_);
    return bless $self, $class;
}

sub discover {
    my $self = shift;

    my $url = $self->user_url();

    my $req = HTTP::Request->new(GET => $url);
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        die "Bad username or password\n" if $res->code == 401;
        die $res->status_line . "\n";
    }

    warn "Success code " . $res->code . " not recognized\n"
        unless $res->code == 200;

    return $res;
}

sub publish {
    my $self = shift;
    my $uuid = shift;
    my $fh = shift;
    my $parentUuid = shift;
    my $ticketTypes = shift;

    my $url = $self->collection_url($uuid);
    $url = sprintf("%s?parent=%s", $url, $parentUuid) if $parentUuid;

    my $req = HTTP::Request->new(PUT => $url);
    $req->content_type(Cosmo::Constants::MEDIA_TYPE_EIMML);
    $req->header(HEADER_TICKET_TYPE, $ticketTypes) if @$ticketTypes;
    while (defined($_ = $fh->getline())) {
        $req->add_content($_);
    }
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        die "Bad username or password\n" if $res->code == 401;
        die "Collection $uuid does not exist\n" if $res->code == 404;
        die "UUID $uuid is already in use\n" if $res->code == 409;
        die "Parent item is not a collection\n" if $res->code == 412;
        die $res->status_line . "\n";
    }

    warn "Success code " . $res->code . " not recognized\n"
        unless $res->code == 201;

    my $pr = Cosmo::MC::PubResponse->new();
    $pr->token($res->header(HEADER_SYNC_TOKEN));

    my $val = $res->header(HEADER_TICKET);
    if ($val) {
        for my $t (split /, /, $val) {
            my ($id, $key) = split /=/, $t;
            $pr->add_ticket($id, $key);
        }
    }

    return $pr;
}

sub update {
    my $self = shift;
    my $uuid = shift;
    my $fh = shift;
    my $token = shift;

    $self->_require_ticket_type(TICKET_TYPE_READ_WRITE) if $self->{ticket};

    my $url = $self->collection_url($uuid);

    my $req = HTTP::Request->new(POST => $url);
    $req->content_type(Cosmo::Constants::MEDIA_TYPE_EIMML);
    $req->header(HEADER_SYNC_TOKEN, $token);
    while (defined($_ = $fh->getline())) {
        $req->add_content($_);
    }
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        if ($res->code == 423) {
            print "The collection is locked for an in-progress update. Try again momentarily.\n";
            return;
        }

        die "Bad username or password\n" if $res->code == 401;
        die "Collection $uuid does not exist\n" if $res->code == 404;
        die "Parent item is not a collection\n" if $res->code == 412;
        die $res->status_line . "\n";
    }

    if ($res->code == 205) {
        print "The collection has been updated since you last synchronized. You must re-synchronize again before updating.\n";
        return;
    }

    warn "Success code " . $res->code . " not recognized\n"
        unless $res->code == 204;

    return $res->header(HEADER_SYNC_TOKEN);
}

sub subscribe {
    my $self = shift;
    my $uuid = shift;

    return $self->synchronize($uuid, undef);
}

sub synchronize {
    my $self = shift;
    my $uuid = shift;
    my $token = shift;

    $self->_require_ticket_type([TICKET_TYPE_READ_WRITE,
                                 TICKET_TYPE_READ_ONLY])
        if $token && $self->{ticket};

    my $url = $self->collection_url($uuid);

    my $req = HTTP::Request->new(GET => $url);
    $req->header(HEADER_SYNC_TOKEN, $token) if $token;
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        die "Bad username or password\n" if $res->code == 401;
        die "Collection $uuid does not exist\n" if $res->code == 404;
        die "Item $uuid is not a collection\n" if $res->code == 412;
        die $res->status_line . "\n";
    }

    warn "Success code " . $res->code . " not recognized\n"
        unless $res->code == 200;

    my $sr = Cosmo::MC::SubResponse->new();
    $sr->token($res->header(HEADER_SYNC_TOKEN));
    $sr->collection($res->content);
    $sr->ticket_type($res->header(HEADER_TICKET_TYPE));

    $self->{ticket_type} = $sr->ticket_type();

    return $sr;
}

sub delete {
    my $self = shift;
    my $uuid = shift;

    $self->_require_ticket_type(TICKET_TYPE_READ_WRITE) if $self->{ticket};

    my $url = $self->collection_url($uuid);

    my $req = HTTP::Request->new(DELETE => $url);
    print $req->as_string . "\n" if $self->{debug};

    my $res = $self->request($req);
    print $res->as_string . "\n" if $self->{debug};

    if (! $res->is_success) {
        die "Bad username or password\n" if $res->code == 401;
        die "Collection $uuid does not exist\n" if $res->code == 404;
        die "Item $uuid is not a collection\n" if $res->code == 412;
        die $res->status_line . "\n";
    }

    warn "Success code " . $res->code . " not recognized\n"
        unless $res->code == 204;

    return 1;
}

sub mc_url {
    my $self = shift;

    return sprintf("%s/mc", $self->server_url);
}

sub user_url {
    my $self = shift;

    return sprintf("%s/user/%s", $self->mc_url, $self->{username});
}

sub collection_url {
    my $self = shift;
    my $uuid = shift;

    return sprintf("%s/collection/%s", $self->mc_url, $uuid);
}

sub _require_ticket_type {
    my $self = shift;
    my $ticket_types = ref($_[0]) ? shift : [ shift ];
    my $found = 0;
    for my $type (@$ticket_types) {
        if ($self->{ticket_type} && $self->{ticket_type} eq $type) {
            $found++;
            last;
        }
    }
    die sprintf("Ticket is not of required ticket types %s\n",
                join ", ", @$ticket_types) unless $found;
}

package Cosmo::MC::PubResponse;

sub new {
    my $class = shift;
    my $self = {
        token => undef,
        tickets => {},
    };
    return bless $self, $class;
}

sub token {
    my $self = shift;
    $self->{token} = shift if @_;
    return $self->{token};
}

sub tickets {
    my $self = shift;
    return $self->{tickets};
}

sub add_ticket {
    my $self = shift;
    my $id = shift;
    my $key = shift;
    $self->{tickets}->{$id} = $key;
    return $key;
}

sub ticket {
    my $self = shift;
    my $id = shift;
    return $self->{tickets}->{$id};
}

package Cosmo::MC::SubResponse;

sub new {
    my $class = shift;
    my $self = {
        token => undef,
        collection => undef,
    };
    return bless $self, $class;
}

sub token {
    my $self = shift;
    $self->{token} = shift if @_;
    return $self->{token};
}

sub collection {
    my $self = shift;
    $self->{collection} = shift if @_;
    return $self->{collection};
}

sub ticket_type {
    my $self = shift;
    $self->{ticket_type} = shift if @_;
    return $self->{ticket_type};
}

1;
