package com.app.chat_app.core.mediator;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.mediator.cqrs.Query;
import com.app.chat_app.core.mediator.cqrs.QueryHandler;
import com.app.chat_app.core.mediator.pipeline.PipelineBehavior;
import com.app.chat_app.core.mediator.pipeline.RequestHandlerDelegate;

@Component
public class SpringMediator implements Mediator
{
    private final ApplicationContext context;
    private final List<PipelineBehavior> behaviors;


    public SpringMediator(ApplicationContext context, List<PipelineBehavior> behaviors) {
        this.context = context;
        this.behaviors = behaviors.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
    }

    @Override
    public <R> R send(Command<R> command) {
        var handler = (CommandHandler<Command<R>, R>) resolveHandler(command.getClass(), CommandHandler.class);

        return invokePipeline(command, () -> handler.handle(command));
    }

    @Override
    public <R> R send(Query<R> query) {
        var handler = (QueryHandler<Query<R>, R>) resolveHandler(query.getClass(), QueryHandler.class);

        return invokePipeline(query, () -> handler.handle(query));
    }

    // Hangi command/query -> hangi handler?
    private Object resolveHandler(Class<?> requestType, Class<?> handlerInterface) {
        // TODO: Add Caching

        // Tüm handlerları gez, komutla/query ile uyuşanı dön.
        // NOT: .as(handlerInterface) tüm tip hiyerarşisini geziyor.
        // Sadece .getInterfaces() kullansaydık @Transactional gibi annotation'ların
        // tetiklediği CGLIB proxy'leri ($$SpringCGLIB$$) handler'ı gizlerdi —
        // proxy sınıfı CommandHandler'ı direkt implement etmez, üst sınıfı eder.
        String[] beanNames = context.getBeanNamesForType(handlerInterface);

        for (String beanName : beanNames)
        {
            Class<?> beanClass = context.getType(beanName);
            if (beanClass == null) continue;

            ResolvableType resolved = ResolvableType.forClass(beanClass).as(handlerInterface);
            if (resolved == ResolvableType.NONE) continue;

            Class<?> firstGeneric = resolved.getGeneric(0).resolve();
            if (firstGeneric != null && firstGeneric.equals(requestType))
                return context.getBean(beanName);
        }

        throw new IllegalStateException("Handler bulunamadı: " + requestType.getSimpleName());
    }


    // zincir başlatıcı
    private <R> R invokePipeline(Object request, RequestHandlerDelegate<R> handlerInvocation)
    {
        RequestHandlerDelegate<R> next = handlerInvocation; // Handler'ın kendisi

        for(int i = behaviors.size() - 1; i >= 0; i--) // Sıraya göre tersten Behaviorları çağır.
        {
            PipelineBehavior behavior = behaviors.get(i);
            if(!behavior.supports(request)) continue;

            RequestHandlerDelegate<R> current = next;
            next = () -> behavior.handle(request, current);
        }

        return next.invoke(); // handlerın kendisi
    }
}
