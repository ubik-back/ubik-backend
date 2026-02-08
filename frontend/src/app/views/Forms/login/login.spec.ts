import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { LoginComponent } from './login.component';
import { LoginService } from './services/login.service';
import { of, throwError } from 'rxjs';
import { OAuthProvider } from './types/login.types';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockLoginService: jasmine.SpyObj<LoginService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create mock services
    mockLoginService = jasmine.createSpyObj('LoginService', [
      'validateForm',
      'login',
      'loginWithOAuth',
      'storeAuthToken',
    ]);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('form updates', () => {
    it('should update email field', () => {
      const email = 'test@example.com';
      component.updateField('email', email);
      
      expect(component.formData().email).toBe(email);
    });

    it('should update password field', () => {
      const password = 'password123';
      component.updateField('password', password);
      
      expect(component.formData().password).toBe(password);
    });
  });

  describe('form submission', () => {
    it('should not submit if already submitting', () => {
      component.isSubmitting.set(true);
      component.submitLogin();
      
      expect(mockLoginService.validateForm).not.toHaveBeenCalled();
    });

    it('should show validation errors for invalid form', () => {
      const validationErrors = [
        { field: 'email', message: 'Email is required' },
      ];
      mockLoginService.validateForm.and.returnValue(validationErrors);
      
      component.submitLogin();
      
      expect(component.errors().length).toBeGreaterThan(0);
      expect(mockLoginService.login).not.toHaveBeenCalled();
    });

    it('should call login service on valid form submission', () => {
      mockLoginService.validateForm.and.returnValue([]);
      mockLoginService.login.and.returnValue(
        of({
          success: true,
          message: 'Login successful',
          token: 'test-token',
          redirectUrl: '/home',
        })
      );
      mockRouter.navigate.and.returnValue(Promise.resolve(true));
      
      component.updateField('email', 'test@example.com');
      component.updateField('password', 'password123');
      component.submitLogin();
      
      expect(mockLoginService.login).toHaveBeenCalled();
    });

    it('should handle login success', (done) => {
      mockLoginService.validateForm.and.returnValue([]);
      mockLoginService.login.and.returnValue(
        of({
          success: true,
          message: 'Login successful',
          token: 'test-token',
          redirectUrl: '/home',
        })
      );
      mockRouter.navigate.and.returnValue(Promise.resolve(true));
      
      component.updateField('email', 'test@example.com');
      component.updateField('password', 'password123');
      component.submitLogin();
      
      setTimeout(() => {
        expect(mockLoginService.storeAuthToken).toHaveBeenCalledWith('test-token');
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
        done();
      }, 100);
    });

    it('should handle login error', (done) => {
      mockLoginService.validateForm.and.returnValue([]);
      mockLoginService.login.and.returnValue(
        throwError(() => ({ message: 'Login failed' }))
      );
      
      component.updateField('email', 'test@example.com');
      component.updateField('password', 'password123');
      component.submitLogin();
      
      setTimeout(() => {
        expect(component.errors().length).toBeGreaterThan(0);
        expect(component.isSubmitting()).toBeFalsy();
        done();
      }, 100);
    });
  });

  describe('OAuth login', () => {
    it('should not submit if already submitting', () => {
      component.isSubmitting.set(true);
      component.loginWithOAuth(OAuthProvider.GOOGLE);
      
      expect(mockLoginService.loginWithOAuth).not.toHaveBeenCalled();
    });

    it('should call OAuth login service', () => {
      mockLoginService.loginWithOAuth.and.returnValue(
        of({
          success: true,
          message: 'OAuth login successful',
          token: 'oauth-token',
          redirectUrl: '/home',
        })
      );
      mockRouter.navigate.and.returnValue(Promise.resolve(true));
      
      component.loginWithOAuth(OAuthProvider.GOOGLE);
      
      expect(mockLoginService.loginWithOAuth).toHaveBeenCalledWith(OAuthProvider.GOOGLE);
    });
  });

  describe('navigation', () => {
    it('should navigate to register page', () => {
      mockRouter.navigate.and.returnValue(Promise.resolve(true));
      
      component.navigateToRegister();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/register']);
    });
  });

  describe('error handling', () => {
    it('should get field error', () => {
      component.errors.set([
        { field: 'email', message: 'Email is required' },
      ]);
      
      expect(component.getFieldError('email')).toBe('Email is required');
    });

    it('should return null for non-existent error', () => {
      component.errors.set([]);
      
      expect(component.getFieldError('email')).toBeNull();
    });

    it('should check if field has error', () => {
      component.errors.set([
        { field: 'email', message: 'Email is required' },
      ]);
      
      expect(component.hasFieldError('email')).toBeTruthy();
      expect(component.hasFieldError('password')).toBeFalsy();
    });
  });
});
