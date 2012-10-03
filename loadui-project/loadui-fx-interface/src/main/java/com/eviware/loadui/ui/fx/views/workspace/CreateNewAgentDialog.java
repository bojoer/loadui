package com.eviware.loadui.ui.fx.views.workspace;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPaneBuilder;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.api.discovery.AgentDiscovery;
import com.eviware.loadui.api.discovery.AgentDiscovery.AgentReference;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.ErrorDialog;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CreateNewAgentDialog extends ConfirmationDialog
{
	private static final Pattern URL_PATTERN = Pattern
			.compile( "^(?<trimmed>(?<protocol>https?://)?(?<host>([A-Za-z0-9\\.-]+))(?<port>:[0-9]{1,5})?)/?$" );

	private final ObservableSet<AgentReference> selectedAgentRefs = FXCollections.observableSet();
	private final TextField agentNameField = new TextField();
	private final TextField agentUrlField = new TextField();
	private final WorkspaceItem workspace;

	public CreateNewAgentDialog( final WorkspaceItem workspace, final Node owner )
	{
		super( owner, "Add new Agent", "Add" );

		this.workspace = workspace;

		agentNameField.disableProperty().bind( Bindings.isEmpty( selectedAgentRefs ).not() );
		agentUrlField.disableProperty().bind( Bindings.isEmpty( selectedAgentRefs ).not() );

		setOnConfirm( new ConfirmHandler( owner ) );

		getItems().setAll( new Label( "Agent name" ), agentNameField, new Label( "Agent URL" ), agentUrlField );
		addDetectedAgents();
	}

	private void addDetectedAgents()
	{
		try
		{
			AgentDiscovery discovery = BeanInjector.getBeanFuture( AgentDiscovery.class ).get( 100, TimeUnit.MILLISECONDS );
			Iterable<AgentReference> newAgents = Iterables.filter( discovery.getDiscoveredAgents(),
					new Predicate<AgentReference>()
					{
						@Override
						public boolean apply( AgentReference agentReference )
						{
							return validateUnique( filterUrl( agentReference.getUrl() ) );
						}
					} );

			if( !Iterables.isEmpty( newAgents ) )
			{
				Iterable<Node> agentCheckBoxes = Iterables.transform( newAgents, new Function<AgentReference, Node>()
				{
					@Override
					public Node apply( final AgentReference agentReference )
					{
						final CheckBox checkBox = CheckBoxBuilder.create()
								.text( String.format( "%s (%s)", agentReference.getDefaultLabel(), agentReference.getUrl() ) )
								.build();
						checkBox.setOnAction( new EventHandler<ActionEvent>()
						{
							@Override
							public void handle( ActionEvent event )
							{
								if( checkBox.isSelected() )
								{
									selectedAgentRefs.add( agentReference );
								}
								else
								{
									selectedAgentRefs.remove( agentReference );
								}
							}
						} );

						return checkBox;
					}
				} );

				getItems().addAll(
						new Label( "Agents detected in your network" ),
						ScrollPaneBuilder
								.create()
								.content(
										VBoxBuilder.create().spacing( 6 ).padding( new Insets( 2 ) )
												.children( Lists.newArrayList( agentCheckBoxes ) ).build() ).build() );
			}
		}
		catch( InterruptedException | ExecutionException | TimeoutException e )
		{
			// Ignore failure.
		}
	}

	private static String filterUrl( String url )
	{
		Matcher matcher = URL_PATTERN.matcher( url );
		if( matcher.matches() )
		{
			url = matcher.group( "trimmed" );
			String protocol = matcher.group( "protocol" );
			String port = matcher.group( "port" );

			if( Strings.isNullOrEmpty( protocol ) )
			{
				url = "https://" + url;
			}
			if( Strings.isNullOrEmpty( port ) )
			{
				url = url + ":8443";
			}

			return url;
		}

		return null;
	}

	private boolean validateName( final String name )
	{
		if( Strings.isNullOrEmpty( name ) )
		{
			return false;
		}

		return Iterables.all( workspace.getAgents(), new Predicate<AgentItem>()
		{
			@Override
			public boolean apply( AgentItem agent )
			{
				return !Objects.equal( name, agent.getLabel() );
			}
		} );
	}

	private boolean validateUnique( final String url )
	{
		return Iterables.all( workspace.getAgents(), new Predicate<AgentItem>()
		{
			@Override
			public boolean apply( AgentItem agent )
			{
				return !Objects.equal( url, filterUrl( agent.getUrl() ) );
			}
		} );
	}

	private final class ConfirmHandler implements EventHandler<ActionEvent>
	{
		private final Node owner;

		private ConfirmHandler( Node owner )
		{
			this.owner = owner;
		}

		@Override
		public void handle( ActionEvent event )
		{
			if( !selectedAgentRefs.isEmpty() )
			{
				for( AgentReference agentReference : selectedAgentRefs )
				{
					workspace.createAgent( agentReference, agentReference.getDefaultLabel() );
				}

				close();
			}
			else
			{
				String name = agentNameField.getText();
				String url = agentUrlField.getText();
				String filteredUrl = filterUrl( url );

				if( !validateName( name ) )
				{
					new ErrorDialog( owner, "Invalid Agent name",
							"The name '%s' is not valid. Agent names must be unique, and consist of at least one character.",
							name ).show();
				}
				else if( filteredUrl == null )
				{
					new ErrorDialog( owner, "Invalid Agent URL", "The URL '%s' is not valid.", url ).show();
				}
				else if( !validateUnique( filteredUrl ) )
				{
					new ErrorDialog( owner, "Agent already existis",
							"The given URL: '%s' points to an Agent that has already been added.", filteredUrl ).show();
				}
				else
				{
					workspace.createAgent( filteredUrl, name );
					close();
				}
			}
		}
	}
}