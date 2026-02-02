import { useState } from 'react';
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
  FieldSeparator,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { AlertDescription } from "./ui/alert"
import { verifyCode, resendCode } from "@/lib/api"

interface EmailVerificationPageProps {
  email: string;
  onVerificationSuccess: () => void;
  onBackToLogin: () => void;
  className?: string;
}

export function EmailVerificationPage({ 
  email, 
  onVerificationSuccess, 
  onBackToLogin, 
  className, 
  ...props 
}: EmailVerificationPageProps) {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [resendSuccess, setResendSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (!code || code.length < 4) {
      setError('Please enter a valid verification code');
      return;
    }

    setIsLoading(true);
    try {
      await verifyCode(email, code);
      setSuccess(true);
      // Show success message for 1 second then redirect to login
      setTimeout(() => {
        onVerificationSuccess();
      }, 1000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Verification failed. Please check your code and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendCode = async () => {
    setError('');
    setResendSuccess(false);
    setIsResending(true);
    try {
      await resendCode(email);
      setResendSuccess(true);
      // Clear success message after 3 seconds
      setTimeout(() => {
        setResendSuccess(false);
      }, 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to resend code. Please try again.');
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-50 to-neutral-100 flex items-center justify-center p-4">
      <div className={cn("flex flex-col gap-6 w-full max-w-md", className)} {...props}>
        <Card className="overflow-hidden p-0">
          <CardContent className="p-6 md:p-8">
            <FieldGroup>
              <div className="flex flex-col items-center gap-2 text-center mb-6">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mb-4">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-8 w-8 text-primary"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                    />
                  </svg>
                </div>
                <h1 className="text-2xl font-bold">Verify your email</h1>
                <p className="text-muted-foreground text-sm text-balance">
                  We've sent a verification code to
                </p>
                <p className="text-sm font-medium text-foreground">{email}</p>
                <p className="text-muted-foreground text-sm mt-2">
                  the code will expire after 15 mins
                </p>
                
              </div>

              <form onSubmit={handleSubmit} className='flex flex-col gap-3'>
                <Field>
                  <FieldLabel htmlFor="code">Verification Code *</FieldLabel>
                  <Input
                    id="code"
                    type="text"
                    placeholder="Enter 6-digit code"
                    value={code}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '').slice(0, 6);
                      setCode(value);
                    }}
                    maxLength={6}
                    className="text-center text-2xl tracking-widest font-mono"
                    required
                    autoFocus
                  />
                  <FieldDescription>
                    Enter the 6-digit code sent to your email
                  </FieldDescription>
                </Field>

                {error && (
                  <div className="bg-red-50 text-red-900 p-3 rounded-lg border border-red-200">
                    <AlertDescription>{error}</AlertDescription>
                  </div>
                )}

                {success && (
                  <div className="bg-green-50 text-green-900 p-3 rounded-lg border border-green-200">
                    <AlertDescription>
                      Email verified successfully! Redirecting to login...
                    </AlertDescription>
                  </div>
                )}

                {resendSuccess && (
                  <div className="bg-green-50 text-green-900 p-3 rounded-lg border border-green-200">
                    <AlertDescription>
                      Verification code has been resent to your email.
                    </AlertDescription>
                  </div>
                )}

                <Field>
                  <Button 
                    type="submit" 
                    className="w-full" 
                    disabled={isLoading || success || code.length < 6}
                  >
                    {isLoading ? 'Verifying...' : success ? 'Verified!' : 'Verify Email'}
                  </Button>
                </Field>

                <Field className="text-center">
                  <FieldDescription>
                    Didn't receive the code?{' '}
                    <Button
                      variant="link"
                      onClick={handleResendCode}
                      className="p-0 h-auto underline-offset-2"
                      type="button"
                      disabled={isLoading || isResending}
                    >
                      {isResending ? 'Sending...' : 'Resend code'}
                    </Button>
                  </FieldDescription>
                </Field>

                <FieldSeparator className="*:data-[slot=field-separator-content]:bg-card">
                  Or
                </FieldSeparator>

                <FieldDescription className="text-center">
                  <Button
                    variant="link"
                    onClick={onBackToLogin}
                    className="p-0 h-auto underline-offset-2"
                    type="button"
                  >
                    Back to login
                  </Button>
                </FieldDescription>
              </form>
            </FieldGroup>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
