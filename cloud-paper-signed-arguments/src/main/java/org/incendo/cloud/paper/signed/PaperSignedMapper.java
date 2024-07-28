//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.incendo.cloud.paper.signed;

import com.google.gson.JsonElement;
import io.leangen.geantyref.TypeToken;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.signed.SignedArguments;
import org.incendo.cloud.minecraft.signed.SignedGreedyStringParser;
import org.incendo.cloud.minecraft.signed.SignedString;
import org.incendo.cloud.minecraft.signed.SignedStringMapper;
import org.incendo.cloud.paper.PluginMetaHolder;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.jetbrains.annotations.Contract;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type;

@API(status = API.Status.INTERNAL)
@DefaultQualifier(NonNull.class)
public final class PaperSignedMapper implements SignedStringMapper {

    private volatile @MonotonicNonNull ProxyHolder proxies;

    private ProxyHolder proxies() {
        if (this.proxies == null) {
            synchronized (this) {
                if (this.proxies == null) {
                    this.proxies = new ProxyHolder();
                }
            }
        }
        return this.proxies;
    }

    @Override
    public CompletableFuture<ArgumentParseResult<SignedString>> apply(
        final CommandContext<?> ctx,
        final String str
    ) {
        final Map<String, ?> signedArgs;
        try {
            final Object stack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
            final Object signingContext = this.proxies().commandSourceStackProxy.getSigningContext(stack);
            signedArgs = this.proxies().signedArgumentsProxy.arguments(signingContext);
        } catch (final Throwable thr) {
            return ArgumentParseResult.successFuture(SignedString.unsigned(str));
        }
        if (signedArgs.isEmpty()) {
            return ArgumentParseResult.successFuture(SignedString.unsigned(str));
        }
        if (signedArgs.size() != 1) {
            throw new IllegalStateException("Found more signed arguments than expected (" + signedArgs.size() + ")");
        }
        return ArgumentParseResult.successFuture(
            new SignedStringImpl(
                str,
                signedArgs.entrySet().iterator().next().getValue(),
                this
            )
        );
    }

    @Override
    public void registerBrigadier(final CommandManager<?> commandManager, final Object brigadierManager) {
        registerBrigadierGeneric(commandManager, brigadierManager);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <C> void registerBrigadierGeneric(final CommandManager<C> commandManager, final Object brigadierManager) {
        // Paper 1.20+
        if (commandManager.capabilities().contains(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)
            && CraftBukkitReflection.MAJOR_REVISION >= 20) {
            final BukkitBrigadierMapper<C> mapper;
            if (commandManager instanceof BukkitCommandManager) {
                mapper = new BukkitBrigadierMapper<>(
                    ((BukkitCommandManager) commandManager).owningPlugin().getLogger(),
                    (CloudBrigadierManager) brigadierManager);
            } else {
                mapper = new BukkitBrigadierMapper<>(
                    Logger.getLogger(((PluginMetaHolder) commandManager).owningPluginMeta().getName()),
                    (CloudBrigadierManager) brigadierManager);
            }
            mapper.mapSimpleNMS(new TypeToken<SignedGreedyStringParser<C>>() {}, "message", true);
        } else {
            SignedArguments.registerDefaultBrigadierMapping(brigadierManager);
        }
    }

    private static final class SignedStringImpl implements SignedString {
        private final String string;
        private final Object playerChatMessage;
        private final PaperSignedMapper mapper;

        private SignedStringImpl(
            final String string,
            final Object playerChatMessage,
            final PaperSignedMapper mapper
        ) {
            this.string = string;
            this.playerChatMessage = playerChatMessage;
            this.mapper = mapper;
        }

        @Override
        public SignedMessage signedMessage() {
            final Object nativeAdventureView = this.mapper.proxies().playerChatMessageProxy.adventureView(this.playerChatMessage);
            if (nativeAdventureView instanceof SignedMessage) {
                return (SignedMessage) nativeAdventureView;
            } else {
                return this.relocate(nativeAdventureView);
            }
        }

        @Override
        public void sendMessage(final Audience audience, final ChatType.Bound chatType, final Component unsigned) {
            audience.forEachAudience(recipient -> this.sendMessageRaw(recipient, chatType, unsigned));
        }

        private void sendMessageRaw(final Audience audience, final ChatType.Bound chatType, final Component unsigned) {
            final Object nativeComponent;
            if (this.mapper.proxies().nativeAdventureComponent.isInstance(unsigned)) {
                nativeComponent = this.mapper.proxies().paperAdventureProxy.asVanilla(unsigned);
            } else {
                nativeComponent = this.mapper.proxies().paperAdventureProxy.asVanilla(
                    this.toNativeAdventure(unsigned)
                );
            }
            final Object modifiedPlayerChat = this.mapper.proxies().playerChatMessageProxy
                .withUnsignedContent(this.playerChatMessage, nativeComponent);
            final Object nativeAdventureView = this.mapper.proxies().playerChatMessageProxy.adventureView(modifiedPlayerChat);
            if (nativeAdventureView instanceof SignedMessage) {
                audience.sendMessage((SignedMessage) nativeAdventureView, chatType);
            } else {
                final Optional<Player> player = audience.get(Identity.UUID).map(Bukkit::getPlayer);
                if (player.isPresent()) {
                    final Object serverPlayer = this.mapper.proxies().craftPlayerProxy.getHandle(player.get());
                    final Object nativeAdvChatType = this.mapper.proxies().chatTypeProxy.chatType(
                        this.mapper.proxies().keyProxy.key(chatType.type().key().asString())
                    );
                    final Object nativeAdvBoundChatType = this.mapper.proxies().chatTypeProxy.bind(
                        nativeAdvChatType,
                        this.toNativeAdventure(chatType.name()),
                        this.toNativeAdventure(chatType.target())
                    );
                    this.mapper.proxies().serverPlayerProxy.sendChatMessage(
                        serverPlayer,
                        this.mapper.proxies().outgoingChatMessageProxy.create(modifiedPlayerChat),
                        this.mapper.proxies().serverPlayerProxy.isTextFilteringEnabled(serverPlayer),
                        this.mapper.proxies().craftPlayerProxy.toHandle(player.get(), nativeAdvBoundChatType)
                    );
                } else {
                    audience.sendMessage(this.relocate(nativeAdventureView), chatType);
                }
            }
        }

        @Contract("null -> null; !null -> !null")
        private @Nullable Object toNativeAdventure(final @Nullable Component component) {
            if (component == null) {
                return null;
            }
            final JsonElement tree = GsonComponentSerializer.gson().serializeToTree(component);
            return this.mapper.proxies().gsonSerializerProxy.deserializeFromTree(
                this.mapper.proxies().gsonSerializerProxy.gson(),
                tree
            );
        }

        private SignedMessage relocate(final Object nativeAdventureView) {
            final AdventureSignedMessageProxy adventureSignedMessageProxy = this.mapper.proxies().adventureSignedMessageProxy;
            return new SignedMessage() {
                @Override
                public Instant timestamp() {
                    return adventureSignedMessageProxy.timestamp(nativeAdventureView);
                }

                @Override
                public long salt() {
                    return adventureSignedMessageProxy.salt(nativeAdventureView);
                }

                @Override
                public @Nullable Signature signature() {
                    final @Nullable Object signature = adventureSignedMessageProxy.signature(nativeAdventureView);
                    if (signature == null || signature instanceof Signature) {
                        return (Signature) signature;
                    }
                    final byte[] bytes = SignedStringImpl.this.mapper.proxies().signatureProxy.bytes(signature);
                    return () -> bytes;
                }

                @Override
                public @Nullable Component unsignedContent() {
                    final @Nullable Object component = adventureSignedMessageProxy.unsignedContent(nativeAdventureView);
                    if (component == null || component instanceof Component) {
                        return (Component) component;
                    }
                    final JsonElement tree = SignedStringImpl.this.mapper.proxies().gsonSerializerProxy.serializeToTree(
                        SignedStringImpl.this.mapper.proxies().gsonSerializerProxy.gson(),
                        component
                    );
                    return GsonComponentSerializer.gson().deserializeFromTree(tree);
                }

                @Override
                public String message() {
                    return adventureSignedMessageProxy.message(nativeAdventureView);
                }

                @Override
                public Identity identity() {
                    final Object identity = adventureSignedMessageProxy.identity(nativeAdventureView);
                    if (identity instanceof Identity) {
                        return (Identity) identity;
                    }
                    final UUID id = SignedStringImpl.this.mapper.proxies().identityProxy.uuid(identity);
                    return Identity.identity(id);
                }
            };
        }

        @Override
        public String string() {
            return this.string;
        }
    }

    @Proxies(className = "net.minecraft.commands.CommandSourceStack")
    interface CommandSourceStackProxy {

        Object getSigningContext(Object instance);
    }

    @Proxies(className = "net.minecraft.commands.CommandSigningContext$SignedArguments")
    interface SignedArgumentsProxy {

        Map<String, ?> arguments(Object instance);
    }

    @Proxies(className = "net.minecraft.network.chat.PlayerChatMessage")
    interface PlayerChatMessageProxy {

        Object adventureView(Object instance);

        Object withUnsignedContent(Object instance, @Type(className = "net.minecraft.network.chat.Component") Object unsignedContent);
    }

    @Proxies(className = "io.papermc.paper.adventure.PaperAdventure")
    interface PaperAdventureProxy {

        @Static
        Object asVanilla(@Type(className = "net_kyori_adventure_text_Component") Object component);
    }

    @Proxies(className = "net_kyori_adventure_text_serializer_gson_GsonComponentSerializer")
    interface GsonSerializerProxy {
        @Static
        Object gson();

        Object deserializeFromTree(Object instance, JsonElement tree);

        JsonElement serializeToTree(Object instance, @Type(className = "net_kyori_adventure_text_Component") Object component);
    }

    @Proxies(className = "net_kyori_adventure_identity_Identified")
    interface IdentifiedProxy {
        Object identity(Object instance);
    }

    @Proxies(className = "net_kyori_adventure_chat_SignedMessage")
    interface AdventureSignedMessageProxy extends IdentifiedProxy {
        Instant timestamp(Object instance);

        long salt(Object instance);

        @Nullable Object signature(Object instance);

        @Nullable Object unsignedContent(Object instance);

        String message(Object instance);
    }

    @Proxies(className = "net_kyori_adventure_key_Key")
    interface KeyProxy {
        @Static
        Object key(String key);
    }

    @Proxies(className = "net_kyori_adventure_chat_ChatType")
    interface ChatTypeProxy {
        @Static
        Object chatType(@Type(className = "net_kyori_adventure_key_Keyed") Object keyed);

        Object bind(
            Object instance,
            @Type(className = "net_kyori_adventure_text_ComponentLike") Object name,
            @Type(className = "net_kyori_adventure_text_ComponentLike") @Nullable Object target
        );
    }

    @Proxies(className = "net_kyori_adventure_identity_Identity")
    interface IdentityProxy {
        UUID uuid(Object instance);
    }

    @Proxies(className = "net_kyori_adventure_chat_SignedMessage$Signature")
    interface SignatureProxy {
        byte[] bytes(Object instance);
    }

    @Proxies(className = "CB_PKG.entity.CraftPlayer")
    interface CraftPlayerProxy {
        Object getHandle(Object instance);

        Object toHandle(Object instance, @Type(className = "net_kyori_adventure_chat_ChatType$Bound") Object bound);
    }

    @Proxies(className = "net.minecraft.server.level.ServerPlayer")
    interface ServerPlayerProxy {
        void sendChatMessage(
            Object instance,
            @Type(OutgoingChatMessageProxy.class) Object message,
            boolean filterMaskEnabled,
            @Type(className = "net.minecraft.network.chat.ChatType$Bound") Object params
        );

        boolean isTextFilteringEnabled(Object instance);
    }

    @Proxies(className = "net.minecraft.network.chat.OutgoingChatMessage")
    interface OutgoingChatMessageProxy {
        @Static
        Object create(@Type(PlayerChatMessageProxy.class) Object playerChat);
    }

    private static final class ProxyHolder {
        private final CommandSourceStackProxy commandSourceStackProxy;
        private final SignedArgumentsProxy signedArgumentsProxy;
        private final PlayerChatMessageProxy playerChatMessageProxy;
        private final PaperAdventureProxy paperAdventureProxy;
        private final Class<?> nativeAdventureComponent;
        private final GsonSerializerProxy gsonSerializerProxy;
        private final IdentityProxy identityProxy;
        private final AdventureSignedMessageProxy adventureSignedMessageProxy;
        private final SignatureProxy signatureProxy;
        private final ServerPlayerProxy serverPlayerProxy;
        private final CraftPlayerProxy craftPlayerProxy;
        private final ChatTypeProxy chatTypeProxy;
        private final KeyProxy keyProxy;
        private final OutgoingChatMessageProxy outgoingChatMessageProxy;

        private ProxyHolder() {
            final ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar().withClassNamePreprocessor(name -> {
                // avoid getting constants relocated
                if (name.startsWith("net_kyori")) {
                    return name.replace("_", ".");
                }
                return name.replace("CB_PKG", Bukkit.getServer().getClass().getPackage().getName());
            });
            final ReflectionProxyFactory proxyFactory = ReflectionProxyFactory.create(remapper, this.getClass().getClassLoader());
            this.commandSourceStackProxy = proxyFactory.reflectionProxy(CommandSourceStackProxy.class);
            this.signedArgumentsProxy = proxyFactory.reflectionProxy(SignedArgumentsProxy.class);
            this.playerChatMessageProxy = proxyFactory.reflectionProxy(PlayerChatMessageProxy.class);
            this.paperAdventureProxy = proxyFactory.reflectionProxy(PaperAdventureProxy.class);
            this.gsonSerializerProxy = proxyFactory.reflectionProxy(GsonSerializerProxy.class);
            this.adventureSignedMessageProxy = proxyFactory.reflectionProxy(AdventureSignedMessageProxy.class);
            this.identityProxy = proxyFactory.reflectionProxy(IdentityProxy.class);
            this.signatureProxy = proxyFactory.reflectionProxy(SignatureProxy.class);
            this.craftPlayerProxy = proxyFactory.reflectionProxy(CraftPlayerProxy.class);
            this.serverPlayerProxy = proxyFactory.reflectionProxy(ServerPlayerProxy.class);
            this.chatTypeProxy = proxyFactory.reflectionProxy(ChatTypeProxy.class);
            this.outgoingChatMessageProxy = proxyFactory.reflectionProxy(OutgoingChatMessageProxy.class);
            this.keyProxy = proxyFactory.reflectionProxy(KeyProxy.class);
            this.nativeAdventureComponent = CraftBukkitReflection.needClass(remapper.remapClassName("net_kyori_adventure_text_Component"));
        }
    }

}
